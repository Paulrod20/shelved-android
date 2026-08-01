package com.paulrod.shelved.data

import android.content.Context
import com.paulrod.shelved.data.model.Game
import com.paulrod.shelved.data.model.GameStatus
import com.paulrod.shelved.data.model.Platform
import com.paulrod.shelved.data.model.Profile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONArray
import org.json.JSONObject

/** Small, device-local store. The JSON shape is intentionally stable and easy to migrate. */
class ShelvedRepository(context: Context) {
    private val preferences = context.getSharedPreferences("shelved", Context.MODE_PRIVATE)
    private val _games = MutableStateFlow(readGames())
    private val _profile = MutableStateFlow(readProfile())

    val games: StateFlow<List<Game>> = _games
    val profile: StateFlow<Profile> = _profile

    fun addGame(game: Game) {
        if (_games.value.any { it.id == game.id }) return
        saveGames(listOf(game) + _games.value)
    }

    fun updateGame(game: Game) = saveGames(_games.value.map { if (it.id == game.id) game else it })

    fun updateProfile(profile: Profile) {
        _profile.value = profile
        preferences.edit().putString(PROFILE_KEY, profile.toJson().toString()).apply()
    }

    private fun saveGames(games: List<Game>) {
        _games.value = games
        preferences.edit().putString(GAMES_KEY, JSONArray().apply { games.forEach { put(it.toJson()) } }.toString()).apply()
    }

    private fun readGames(): List<Game> = runCatching {
        val array = JSONArray(preferences.getString(GAMES_KEY, "[]"))
        List(array.length()) { array.getJSONObject(it).toGame() }
    }.getOrDefault(emptyList())

    private fun readProfile(): Profile = runCatching {
        JSONObject(preferences.getString(PROFILE_KEY, "{}") ?: "{}").toProfile()
    }.getOrDefault(Profile())

    private fun Game.toJson() = JSONObject().apply {
        put("id", id); put("name", name); put("coverImageUrl", coverImageUrl)
        put("status", status.name); put("hoursPlayed", hoursPlayed); put("notes", notes)
        put("released", released); put("playtime", playtime); put("description", description)
        put("platforms", JSONArray(platforms))
    }

    private fun JSONObject.toGame() = Game(
        id = getString("id"),
        name = getString("name"),
        coverImageUrl = optNullableString("coverImageUrl"),
        status = runCatching { GameStatus.valueOf(optString("status")) }.getOrDefault(GameStatus.BACKLOG),
        hoursPlayed = if (has("hoursPlayed") && !isNull("hoursPlayed")) getInt("hoursPlayed") else null,
        notes = optNullableString("notes"),
        released = optNullableString("released"),
        playtime = if (has("playtime") && !isNull("playtime")) getInt("playtime") else null,
        platforms = optJSONArray("platforms")?.let { array -> List(array.length()) { array.getString(it) } }.orEmpty(),
        description = optNullableString("description"),
    )

    private fun Profile.toJson() = JSONObject().apply {
        put("displayName", displayName); put("bio", bio)
        put("favoritePlatforms", JSONArray(favoritePlatforms.map { it.name }))
        put("favoriteGameIds", JSONArray(favoriteGameIds))
    }

    private fun JSONObject.toProfile() = Profile(
        displayName = optString("displayName"),
        bio = optString("bio"),
        favoritePlatforms = readFavoritePlatforms(),
        favoriteGameIds = optJSONArray("favoriteGameIds")?.let { array ->
            List(array.length()) { array.getString(it) }
        }.orEmpty(),
    )

    private fun JSONObject.readFavoritePlatforms(): List<Platform> {
        if (has("favoritePlatforms")) {
            val platforms = optJSONArray("favoritePlatforms") ?: return emptyList()
            return List(platforms.length()) { platforms.optString(it) }
                .mapNotNull(::storedPlatform)
                .distinct()
        }

        return listOfNotNull(optNullableString("favoritePlatform")?.let(::storedPlatform))
    }

    private fun storedPlatform(value: String): Platform? = when (value) {
        "SWITCH" -> Platform.NINTENDO
        "STEAM_DECK", "ROG_ALLY", "LEGION_GO", "XBOX_ALLY_X" -> Platform.HANDHELD
        else -> runCatching { Platform.valueOf(value) }.getOrNull()
    }

    private fun JSONObject.optNullableString(key: String): String? =
        if (!has(key) || isNull(key)) null else optString(key).takeIf { it.isNotBlank() }

    private companion object {
        const val GAMES_KEY = "games"
        const val PROFILE_KEY = "profile"
    }
}
