export interface IgdbGame {
  id: number;
  name: string;
  cover?: {image_id?: string};
  first_release_date?: number;
  platforms?: Array<{name?: string}>;
  summary?: string;
}

export interface IgdbTimeToBeat {
  game_id: number;
  normally?: number;
}

export interface ShelvedGame {
  id: string;
  name: string;
  coverImageUrl: string | null;
  released: string | null;
  playtime: number | null;
  platforms: string[];
  description: string | null;
}

export function sanitizeSearchQuery(query: string | null): string {
  if (query === null) throw new ApiError(400, "A search query is required.");
  const normalized = query.trim().replace(/[\u0000-\u001f\u007f]/g, " ");
  if (normalized.length === 0) throw new ApiError(400, "Search query cannot be empty.");
  if (normalized.length > 80) throw new ApiError(400, "Search query is too long.");
  return normalized;
}

export function escapeApicalypseString(value: string): string {
  return value.replace(/\\/g, "\\\\").replace(/"/g, '\\"');
}

export function parseShelvedGameId(value: string): number {
  if (!/^igdb:[1-9]\d*$/.test(value)) throw new ApiError(400, "Invalid game ID.");
  return Number(value.substring("igdb:".length));
}

export function toShelvedGame(game: IgdbGame, timeToBeat?: IgdbTimeToBeat): ShelvedGame {
  const imageId = game.cover?.image_id;
  const playtimeSeconds = timeToBeat?.normally;
  return {
    id: `igdb:${game.id}`,
    name: game.name,
    coverImageUrl: imageId ? `https://images.igdb.com/igdb/image/upload/t_cover_big/${imageId}.jpg` : null,
    released: game.first_release_date ? new Date(game.first_release_date * 1000).toISOString().slice(0, 10) : null,
    playtime: playtimeSeconds ? Math.max(1, Math.round(playtimeSeconds / 3600)) : null,
    platforms: game.platforms?.map((platform) => platform.name).filter((name): name is string => Boolean(name)) ?? [],
    description: game.summary?.trim() || null,
  };
}

export class ApiError extends Error {
  constructor(readonly status: number, message: string) {
    super(message);
  }
}
