package com.paulrod.shelved.data

import com.paulrod.shelved.BuildConfig
import com.paulrod.shelved.data.model.Game
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import org.json.JSONObject

interface GameCatalog {
    fun search(query: String): List<Game>
    fun details(game: Game): Game
}

class RawgApi : GameCatalog {
    private val cache = object : LinkedHashMap<String, List<Game>>(50, .75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, List<Game>>?) = size > 50
    }

    override fun search(query: String): List<Game> {
        val normalized = query.trim().lowercase()
        if (normalized.isEmpty()) return emptyList()
        synchronized(cache) { cache[normalized]?.let { return it } }
        check(BuildConfig.RAWG_API_KEY.isNotBlank()) {
            "RAWG API key is missing. Add RAWG_API_KEY=… to ~/.gradle/gradle.properties."
        }
        val encoded = URLEncoder.encode(query.trim(), Charsets.UTF_8.name())
        val json = request("/games?search=$encoded&page_size=20")
        val results = json.optJSONArray("results") ?: return emptyList()
        val games = List(results.length()) { index -> results.getJSONObject(index).toGame() }
        synchronized(cache) { cache[normalized] = games }
        return games
    }

    override fun details(game: Game): Game {
        check(BuildConfig.RAWG_API_KEY.isNotBlank()) {
            "RAWG API key is missing. Add RAWG_API_KEY to local.properties."
        }
        return request("/games/${game.id}").toGame()
    }

    private fun request(path: String): JSONObject {
        val separator = if ('?' in path) '&' else '?'
        val connection = URL("https://api.rawg.io/api$path${separator}key=${BuildConfig.RAWG_API_KEY}")
            .openConnection() as HttpURLConnection
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000
        connection.setRequestProperty("Accept", "application/json")
        return try {
            if (connection.responseCode !in (200..299)) error("Game database returned ${connection.responseCode}.")
            JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
        } finally {
            connection.disconnect()
        }
    }

    private fun JSONObject.toGame(): Game {
        val platformArray = optJSONArray("platforms")
        return Game(
            id = getInt("id").toString(),
            name = getString("name"),
            coverImageUrl = optString("background_image").takeIf { it.isNotBlank() && it != "null" },
            released = optString("released").takeIf { it.isNotBlank() && it != "null" },
            playtime = optInt("playtime").takeIf { it > 0 },
            platforms = platformArray?.let { array ->
                List(array.length()) { array.getJSONObject(it).getJSONObject("platform").getString("name") }
            }.orEmpty(),
            description = optString("description_raw").takeIf { it.isNotBlank() },
        )
    }
}
