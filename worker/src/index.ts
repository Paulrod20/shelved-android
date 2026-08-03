import {verifyAppCheckToken} from "./appCheck";
import {ApiError} from "./catalog";
import {Env} from "./env";
import {IgdbClient} from "./igdb";
import {routeCatalogRequest} from "./routes";
import {TwitchTokenProvider} from "./twitch";

const twitchTokens = new TwitchTokenProvider();

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

      const result = await routeCatalogRequest(url, new IgdbClient(env, twitchTokens));
      const response = json(result.value, 200, result.cacheSeconds);
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

function json(value: unknown, status = 200, cacheSeconds?: number): Response {
  const headers = new Headers({"Content-Type": "application/json; charset=utf-8"});
  headers.set("Cache-Control", cacheSeconds ? `public, max-age=${cacheSeconds}` : "no-store");
  return new Response(JSON.stringify(value), {status, headers});
}
