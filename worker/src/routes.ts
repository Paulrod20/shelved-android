import {ApiError, sanitizeSearchQuery, ShelvedGame} from "./catalog";
import {GameCatalogClient} from "./igdb";

export interface CatalogRouteResult {
  cacheSeconds: number;
  value: {games: ShelvedGame[]} | {game: ShelvedGame};
}

export async function routeCatalogRequest(
  url: URL,
  catalog: GameCatalogClient,
): Promise<CatalogRouteResult> {
  if (url.pathname === "/v1/search") {
    const query = sanitizeSearchQuery(url.searchParams.get("query"));
    return {value: {games: await catalog.search(query)}, cacheSeconds: 600};
  }
  if (url.pathname.startsWith("/v1/games/")) {
    const id = decodeURIComponent(url.pathname.substring("/v1/games/".length));
    return {value: {game: await catalog.details(id)}, cacheSeconds: 86_400};
  }
  throw new ApiError(404, "Not found.");
}
