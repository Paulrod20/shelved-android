import {verifyAppCheckToken} from "./appCheck";
import {
  ApiError,
  escapeApicalypseString,
  IgdbGame,
  IgdbTimeToBeat,
  parseShelvedGameId,
  sanitizeSearchQuery,
  ShelvedGame,
  toShelvedGame,
} from "./catalog";

interface Env {
  CATALOG_RATE_LIMITER: RateLimit;
  FIREBASE_APP_ID: string;
  FIREBASE_PROJECT_NUMBER: string;
  TWITCH_CLIENT_ID: string;
  TWITCH_CLIENT_SECRET: string;
}

interface CachedToken {
  expiresAtMillis: number;
  value: string;
}

let cachedToken: CachedToken | null = null;
let pendingToken: Promise<string> | null = null;

export default {
  async fetch(request, env, context): Promise<Response> {
    try {
      const url = new URL(request.url);
      if (request.method === "GET" && url.pathname === "/health") return json({status: "ok"});
      if (request.method !== "GET") throw new ApiError(405, "Method not allowed.");

      const appId = await authenticate(request, env);
      const cacheKey = new Request(url.toString(), {method: "GET"});
      const cached = await caches.default.match(cacheKey);
      if (cached) return cached;

      const rateLimit = await env.CATALOG_RATE_LIMITER.limit({key: appId});
      if (!rateLimit.success) throw new ApiError(429, "Too many requests. Try again shortly.");

      let response: Response;
      if (url.pathname === "/v1/search") {
        const query = sanitizeSearchQuery(url.searchParams.get("query"));
        response = json({games: await searchGames(query, env)}, 200, 600);
      } else if (url.pathname.startsWith("/v1/games/")) {
        const id = decodeURIComponent(url.pathname.substring("/v1/games/".length));
        response = json({game: await gameDetails(id, env)}, 200, 86_400);
      } else {
        throw new ApiError(404, "Not found.");
      }
      context.waitUntil(caches.default.put(cacheKey, response.clone()));
      return response;
    } catch (error) {
      if (error instanceof ApiError) return json({error: error.message}, error.status);
      console.error("Catalog request failed.", error);
      return json({error: "Game search is temporarily unavailable."}, 503);
    }
  },
} satisfies ExportedHandler<Env>;

async function authenticate(request: Request, env: Env): Promise<string> {
  try {
    return await verifyAppCheckToken(
      request.headers.get("X-Firebase-AppCheck"),
      env.FIREBASE_PROJECT_NUMBER,
      env.FIREBASE_APP_ID,
    );
  } catch (error) {
    console.warn("Rejected invalid App Check token.", error);
    throw new ApiError(401, "Unauthorized.");
  }
}

async function searchGames(query: string, env: Env): Promise<ShelvedGame[]> {
  const body = [
    `search "${escapeApicalypseString(query)}"`,
    "fields name,cover.image_id,first_release_date,platforms.name",
    "where version_parent = null",
    "limit 20",
  ].join("; ") + ";";
  return (await requestIgdb<IgdbGame[]>("games", body, env)).map((game) => toShelvedGame(game));
}

async function gameDetails(idValue: string, env: Env): Promise<ShelvedGame> {
  const gameId = parseShelvedGameId(idValue);
  const [games, times] = await Promise.all([
    requestIgdb<IgdbGame[]>(
      "games",
      `fields name,cover.image_id,first_release_date,platforms.name,summary; where id = ${gameId}; limit 1;`,
      env,
    ),
    requestIgdb<IgdbTimeToBeat[]>(
      "game_time_to_beats",
      `fields game_id,normally; where game_id = ${gameId}; limit 1;`,
      env,
    ),
  ]);
  if (!games[0]) throw new ApiError(404, "Game not found.");
  return toShelvedGame(games[0], times[0]);
}

async function requestIgdb<T>(endpoint: string, body: string, env: Env, mayRefresh = true): Promise<T> {
  const token = await accessToken(env);
  const response = await fetch(`https://api.igdb.com/v4/${endpoint}`, {
    method: "POST",
    headers: {
      "Accept": "application/json",
      "Authorization": `Bearer ${token}`,
      "Client-ID": env.TWITCH_CLIENT_ID,
    },
    body,
    signal: AbortSignal.timeout(10_000),
  });
  if (response.status === 401 && mayRefresh) {
    cachedToken = null;
    return requestIgdb<T>(endpoint, body, env, false);
  }
  if (response.status === 429) throw new ApiError(503, "Game search is busy. Try again shortly.");
  if (!response.ok) {
    console.error("IGDB returned an error response.", {endpoint, status: response.status});
    throw new ApiError(503, "Game search is temporarily unavailable.");
  }
  return response.json<T>();
}

async function accessToken(env: Env): Promise<string> {
  const now = Date.now();
  if (cachedToken && cachedToken.expiresAtMillis > now + 60_000) return cachedToken.value;
  if (pendingToken) return pendingToken;
  pendingToken = requestAccessToken(env);
  try {
    return await pendingToken;
  } finally {
    pendingToken = null;
  }
}

async function requestAccessToken(env: Env): Promise<string> {
  const requestedAt = Date.now();
  const url = new URL("https://id.twitch.tv/oauth2/token");
  url.search = new URLSearchParams({
    client_id: env.TWITCH_CLIENT_ID,
    client_secret: env.TWITCH_CLIENT_SECRET,
    grant_type: "client_credentials",
  }).toString();
  const response = await fetch(url, {method: "POST", signal: AbortSignal.timeout(10_000)});
  if (!response.ok) {
    console.error("Twitch rejected the IGDB credentials.", {status: response.status});
    throw new ApiError(503, "IGDB credentials are not configured correctly.");
  }
  const result = await response.json<{access_token?: string; expires_in?: number}>();
  if (!result.access_token || !result.expires_in) throw new ApiError(503, "IGDB returned an invalid access token.");
  cachedToken = {
    value: result.access_token,
    expiresAtMillis: requestedAt + result.expires_in * 1000,
  };
  return cachedToken.value;
}

function json(value: unknown, status = 200, cacheSeconds?: number): Response {
  const headers = new Headers({"Content-Type": "application/json; charset=utf-8"});
  if (cacheSeconds) headers.set("Cache-Control", `public, max-age=${cacheSeconds}`);
  else headers.set("Cache-Control", "no-store");
  return new Response(JSON.stringify(value), {status, headers});
}
