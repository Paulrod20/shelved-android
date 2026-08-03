import assert from "node:assert/strict";
import test from "node:test";
import {validateClaims} from "../src/appCheck";
import {
  escapeApicalypseString,
  parseShelvedGameId,
  sanitizeSearchQuery,
  toShelvedGame,
} from "../src/catalog";

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
