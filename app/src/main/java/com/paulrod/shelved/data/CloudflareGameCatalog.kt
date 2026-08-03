package com.paulrod.shelved.data

import com.google.firebase.appcheck.FirebaseAppCheck
import com.paulrod.shelved.data.model.Game
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI
import java.net.URLEncoder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import org.json.JSONObject

class CloudflareGameCatalog(
    private val baseUrl: String = DEFAULT_BASE_URL,
    private val appCheckToken: suspend (forceRefresh: Boolean) -> String = { forceRefresh ->
        FirebaseAppCheck.getInstance().getAppCheckToken(forceRefresh).await().token
    },
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
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

    private suspend fun request(path: String, forceTokenRefresh: Boolean = false): JSONObject {
        val token = appCheckToken(forceTokenRefresh)
        val response = withContext(ioDispatcher) { executeRequest(path, token) }
        if (response.status == HttpURLConnection.HTTP_UNAUTHORIZED && !forceTokenRefresh) {
            return request(path, forceTokenRefresh = true)
        }
        if (response.status !in 200..299) {
            val message = runCatching { JSONObject(response.body).optString("error") }.getOrNull()
                ?.takeIf(String::isNotBlank)
                ?: "Game database returned ${response.status}."
            throw GameCatalogException(response.status, message)
        }
        return JSONObject(response.body)
    }

    private fun executeRequest(path: String, token: String): CatalogResponse {
        val connection = URI.create("$baseUrl$path").toURL().openConnection() as HttpURLConnection
        connection.connectTimeout = REQUEST_TIMEOUT_MILLIS
        connection.readTimeout = REQUEST_TIMEOUT_MILLIS
        connection.setRequestProperty("Accept", "application/json")
        connection.setRequestProperty(APP_CHECK_HEADER, token)
        return try {
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            CatalogResponse(status, stream?.bufferedReader()?.use { it.readText() }.orEmpty())
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
        const val DEFAULT_BASE_URL = "https://shelved-game-api.paulyd0123.workers.dev"
        const val APP_CHECK_HEADER = "X-Firebase-AppCheck"
        const val IGDB_ID_PREFIX = "igdb:"
        const val MAX_CACHED_SEARCHES = 50
        const val REQUEST_TIMEOUT_MILLIS = 10_000
    }
}

private data class CatalogResponse(val status: Int, val body: String)

internal class GameCatalogException(
    val status: Int,
    override val message: String,
) : IOException(message)
