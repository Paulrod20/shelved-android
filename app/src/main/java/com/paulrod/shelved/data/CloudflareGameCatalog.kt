package com.paulrod.shelved.data

import com.google.firebase.appcheck.FirebaseAppCheck
import com.paulrod.shelved.data.model.Game
import java.net.HttpURLConnection
import java.net.URI
import java.net.URLEncoder
import kotlinx.coroutines.tasks.await
import org.json.JSONObject

class CloudflareGameCatalog(
    private val appCheckToken: suspend () -> String = {
        FirebaseAppCheck.getInstance().getAppCheckToken(false).await().token
    },
) : GameCatalog {
    private val searchCache = object : LinkedHashMap<String, List<Game>>(MAX_CACHED_SEARCHES, .75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, List<Game>>?) =
            size > MAX_CACHED_SEARCHES
    }

    override suspend fun search(query: String): List<Game> {
        val normalized = query.trim().lowercase()
        if (normalized.isEmpty()) return emptyList()
        synchronized(searchCache) { searchCache[normalized]?.let { return it } }

        val encodedQuery = URLEncoder.encode(query.trim(), Charsets.UTF_8.name())
        val result = request("/v1/search?query=$encodedQuery")
        val games = result.optJSONArray("games")?.let { games ->
            List(games.length()) { index -> games.getJSONObject(index).toGame() }
        }.orEmpty()
        synchronized(searchCache) { searchCache[normalized] = games }
        return games
    }

    override suspend fun details(game: Game): Game {
        if (!game.id.startsWith(IGDB_ID_PREFIX)) return game
        val encodedId = URLEncoder.encode(game.id, Charsets.UTF_8.name())
        return request("/v1/games/$encodedId").optJSONObject("game")?.toGame() ?: game
    }

    private suspend fun request(path: String): JSONObject {
        val connection = URI.create("$BASE_URL$path").toURL().openConnection() as HttpURLConnection
        connection.connectTimeout = REQUEST_TIMEOUT_MILLIS
        connection.readTimeout = REQUEST_TIMEOUT_MILLIS
        connection.setRequestProperty("Accept", "application/json")
        connection.setRequestProperty(APP_CHECK_HEADER, appCheckToken())
        return try {
            if (connection.responseCode !in 200..299) {
                error("Game database returned ${connection.responseCode}.")
            }
            JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
        } finally {
            connection.disconnect()
        }
    }

    private fun JSONObject.toGame(): Game = Game(
        id = getString("id"),
        name = getString("name"),
        coverImageUrl = nullableString("coverImageUrl"),
        released = nullableString("released"),
        playtime = optInt("playtime").takeIf { it > 0 },
        platforms = optJSONArray("platforms")?.let { platforms ->
            List(platforms.length()) { index -> platforms.getString(index) }
        }.orEmpty(),
        description = nullableString("description"),
    )

    private fun JSONObject.nullableString(key: String): String? =
        optString(key).takeIf { it.isNotBlank() && it != "null" }

    private companion object {
        const val BASE_URL = "https://shelved-game-api.paulyd0123.workers.dev"
        const val APP_CHECK_HEADER = "X-Firebase-AppCheck"
        const val IGDB_ID_PREFIX = "igdb:"
        const val MAX_CACHED_SEARCHES = 50
        const val REQUEST_TIMEOUT_MILLIS = 10_000
    }
}
