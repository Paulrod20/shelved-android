package com.paulrod.shelved.data

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.paulrod.shelved.data.model.Game
import com.paulrod.shelved.data.model.Platform
import com.paulrod.shelved.data.model.Profile
import com.paulrod.shelved.data.sync.LibrarySnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONArray
import org.json.JSONObject

/** Small, device-local store. The JSON shape is intentionally stable and easy to migrate. */
class ShelvedRepository private constructor(context: Context) :
    LibraryRepository {
    private val preferences = context.getSharedPreferences("shelved", Context.MODE_PRIVATE)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val persistence = OrderedWriteQueue<PersistenceWrite>(
        scope = scope,
        writer = { persist(it) },
        onError = { Log.e(TAG, "Could not persist local library data.", it) },
    )
    private val _games = MutableStateFlow(readGames())
    private val _profile = MutableStateFlow(readProfile())

    override val games: StateFlow<List<Game>> = _games
    override val profile: StateFlow<Profile> = _profile

    override fun addGame(game: Game) {
        if (_games.value.any { it.id == game.id }) return
        saveGames(listOf(game) + _games.value)
    }

    override fun updateGame(game: Game) = saveGames(_games.value.map { if (it.id == game.id) game else it })

    override fun deleteGames(gameIds: Set<String>) {
        if (gameIds.isEmpty()) return
        saveGames(_games.value.filterNot { it.id in gameIds })

        val updatedFavoriteIds = _profile.value.favoriteGameIds.filterNot { it in gameIds }
        if (updatedFavoriteIds != _profile.value.favoriteGameIds) {
            updateProfile(_profile.value.copy(favoriteGameIds = updatedFavoriteIds))
        }
    }

    override fun updateProfile(profile: Profile) {
        _profile.value = profile
        persistence.enqueue(PersistenceWrite.ProfileSnapshot(profile))
    }

    /** Replaces both local models after a cloud restore, while keeping disk writes ordered. */
    override fun replaceLibrary(snapshot: LibrarySnapshot) {
        _games.value = snapshot.games
        _profile.value = snapshot.profile
        persistence.enqueue(PersistenceWrite.GameSnapshot(snapshot.games.toList()))
        persistence.enqueue(PersistenceWrite.ProfileSnapshot(snapshot.profile))
    }

    private fun saveGames(games: List<Game>) {
        _games.value = games
        persistence.enqueue(PersistenceWrite.GameSnapshot(games.toList()))
    }

    @SuppressLint("UseKtx") // The direct API exposes commit success, which the ordered writer must verify.
    private fun persist(write: PersistenceWrite) {
        val editor = preferences.edit()
        when (write) {
            is PersistenceWrite.GameSnapshot -> editor.putString(
                GAMES_KEY,
                JSONArray().apply { write.games.forEach { put(it.toStoredJson()) } }.toString(),
            )
            is PersistenceWrite.ProfileSnapshot -> editor.putString(
                PROFILE_KEY,
                write.profile.toJson().toString(),
            )
        }
        check(editor.commit()) { "SharedPreferences rejected a local data write." }
    }

    private fun readGames(): List<Game> = runCatching {
        val array = JSONArray(preferences.getString(GAMES_KEY, "[]"))
        List(array.length()) { array.getJSONObject(it).toStoredGame() }
    }.getOrDefault(emptyList())

    private fun readProfile(): Profile = runCatching {
        JSONObject(preferences.getString(PROFILE_KEY, "{}") ?: "{}").toProfile()
    }.getOrDefault(Profile())

    private fun Profile.toJson() = JSONObject().apply {
        put("displayName", displayName); put("bio", bio)
        put("profileImagePath", profileImagePath)
        put("favoritePlatforms", JSONArray(favoritePlatforms.map { it.name }))
        put("favoriteGameIds", JSONArray(favoriteGameIds))
    }

    private fun JSONObject.toProfile() = Profile(
        displayName = optString("displayName"),
        bio = optString("bio"),
        profileImagePath = optNullableString("profileImagePath"),
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

    companion object {
        @Volatile
        private var instance: ShelvedRepository? = null

        fun getInstance(context: Context): ShelvedRepository = instance ?: synchronized(this) {
            instance ?: ShelvedRepository(context.applicationContext).also { instance = it }
        }

        private const val TAG = "ShelvedRepository"
        private const val GAMES_KEY = "games"
        private const val PROFILE_KEY = "profile"
    }
}

private sealed interface PersistenceWrite {
    data class GameSnapshot(val games: List<Game>) : PersistenceWrite
    data class ProfileSnapshot(val profile: Profile) : PersistenceWrite
}
