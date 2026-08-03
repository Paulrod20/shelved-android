import assert from "node:assert/strict";
import test from "node:test";
import {validateClaims} from "../src/appCheck";
import {
  ApiError,
  escapeApicalypseString,
  parseShelvedGameId,
  sanitizeSearchQuery,
  ShelvedGame,
  toShelvedGame,
} from "../src/catalog";
import {GameCatalogClient} from "../src/igdb";
import {routeCatalogRequest} from "../src/routes";

test("search queries are trimmed and escaped", () => {
  assert.equal(sanitizeSearchQuery("  Mario\nKart  "), "Mario Kart");
  assert.equal(escapeApicalypseString('The "Game" \\ Deluxe'), 'The \\"Game\\" \\\\ Deluxe');
});

test("only namespaced IGDB IDs are accepted", () => {
  assert.equal(parseShelvedGameId("igdb:123"), 123);
  assert.throws(() => parseShelvedGameId("123"));
});

test("IGDB games map to the stable Shelved shape", () => {
  const game = toShelvedGame({
    id: 123,
    name: "Example",
    cover: {image_id: "cover-id"},
    first_release_date: 946684800,
    platforms: [{name: "PC"}, {name: "PlayStation 5"}],
    summary: "  Description  ",
  }, {game_id: 123, normally: 18_000});

  assert.equal(game.id, "igdb:123");
  assert.equal(game.playtime, 5);
  assert.deepEqual(game.platforms, ["PC", "PlayStation 5"]);
  assert.equal(game.description, "Description");
});

test("App Check claims must target this project and app", () => {
  const claims = {
    aud: ["projects/123"],
    exp: 2_000,
    iss: "https://firebaseappcheck.googleapis.com/123",
    sub: "app-id",
  };
  assert.doesNotThrow(() => validateClaims(claims, "123", "app-id", 1_000));
  assert.throws(() => validateClaims({...claims, sub: "other-app"}, "123", "app-id", 1_000));
  assert.throws(() => validateClaims({...claims, exp: 999}, "123", "app-id", 1_000));
});

test("catalog routes normalize searches and decode game IDs", async () => {
  const calls: string[] = [];
  const game: ShelvedGame = {
    id: "igdb:123",
    name: "Example",
    coverImageUrl: null,
    released: null,
    playtime: null,
    platforms: [],
    description: null,
  };
  const catalog: GameCatalogClient = {
    async search(query) {
      calls.push(`search:${query}`);
      return [game];
    },
    async details(id) {
      calls.push(`details:${id}`);
      return game;
    },
  };

  const search = await routeCatalogRequest(new URL("https://example.test/v1/search?query=%20Mario%20"), catalog);
  const details = await routeCatalogRequest(new URL("https://example.test/v1/games/igdb%3A123"), catalog);

  assert.deepEqual(calls, ["search:Mario", "details:igdb:123"]);
  assert.equal(search.cacheSeconds, 600);
  assert.equal(details.cacheSeconds, 86_400);
});

test("unknown catalog routes return a typed 404", async () => {
  const catalog = {} as GameCatalogClient;
  await assert.rejects(
    routeCatalogRequest(new URL("https://example.test/unknown"), catalog),
    (error: unknown) => error instanceof ApiError && error.status === 404,
  );
});
