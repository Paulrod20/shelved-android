import {
  ApiError,
  escapeApicalypseString,
  IgdbGame,
  IgdbTimeToBeat,
  parseShelvedGameId,
  ShelvedGame,
  toShelvedGame,
} from "./catalog";
import {Env} from "./env";
import {TwitchTokenProvider} from "./twitch";

export interface GameCatalogClient {
  search(query: string): Promise<ShelvedGame[]>;
  details(id: string): Promise<ShelvedGame>;
}

export class IgdbClient implements GameCatalogClient {
  constructor(
    private readonly env: Env,
    private readonly tokens: TwitchTokenProvider,
  ) {}

  async search(query: string): Promise<ShelvedGame[]> {
    const body = [
      `search "${escapeApicalypseString(query)}"`,
      "fields name,cover.image_id,first_release_date,platforms.name",
      "where version_parent = null",
      "limit 20",
    ].join("; ") + ";";
    return (await this.request<IgdbGame[]>("games", body)).map((game) => toShelvedGame(game));
  }

  async details(idValue: string): Promise<ShelvedGame> {
    const gameId = parseShelvedGameId(idValue);
    const [games, times] = await Promise.all([
      this.request<IgdbGame[]>(
        "games",
        `fields name,cover.image_id,first_release_date,platforms.name,summary; where id = ${gameId}; limit 1;`,
      ),
      this.request<IgdbTimeToBeat[]>(
        "game_time_to_beats",
        `fields game_id,normally; where game_id = ${gameId}; limit 1;`,
      ),
    ]);
    if (!games[0]) throw new ApiError(404, "Game not found.");
    return toShelvedGame(games[0], times[0]);
  }

  private async request<T>(endpoint: string, body: string, mayRefresh = true): Promise<T> {
    const token = await this.tokens.get(this.env);
    const response = await fetch(`https://api.igdb.com/v4/${endpoint}`, {
      method: "POST",
      headers: {
        "Accept": "application/json",
        "Authorization": `Bearer ${token}`,
        "Client-ID": this.env.TWITCH_CLIENT_ID,
      },
      body,
      signal: AbortSignal.timeout(10_000),
    });
    if (response.status === 401 && mayRefresh) {
      this.tokens.invalidate();
      return this.request<T>(endpoint, body, false);
    }
    if (response.status === 429) throw new ApiError(503, "Game search is busy. Try again shortly.");
    if (!response.ok) {
      console.error("IGDB returned an error response.", {endpoint, status: response.status});
      throw new ApiError(503, "Game search is temporarily unavailable.");
    }
    return response.json<T>();
  }
}
