package com.paulrod.shelved.data

import com.paulrod.shelved.data.model.Game
import com.paulrod.shelved.data.model.Profile
import com.paulrod.shelved.data.sync.LibrarySnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Process-local library used by Try mode. Nothing is written to disk. */
class InMemoryLibraryRepository : LibraryRepository {
    private val mutableGames = MutableStateFlow<List<Game>>(emptyList())
    private val mutableProfile = MutableStateFlow(Profile())

    override val games: StateFlow<List<Game>> = mutableGames
    override val profile: StateFlow<Profile> = mutableProfile

    override fun addGame(game: Game) {
        if (mutableGames.value.any { it.id == game.id }) return
        mutableGames.value = listOf(game) + mutableGames.value
    }

    override fun updateGame(game: Game) {
        mutableGames.value = mutableGames.value.map { if (it.id == game.id) game else it }
    }

    override fun deleteGames(gameIds: Set<String>) {
        if (gameIds.isEmpty()) return
        mutableGames.value = mutableGames.value.filterNot { it.id in gameIds }
        val favoriteIds = mutableProfile.value.favoriteGameIds.filterNot { it in gameIds }
        if (favoriteIds != mutableProfile.value.favoriteGameIds) {
            mutableProfile.value = mutableProfile.value.copy(favoriteGameIds = favoriteIds)
        }
    }

    override fun updateProfile(profile: Profile) {
        mutableProfile.value = profile
    }

    override fun replaceLibrary(snapshot: LibrarySnapshot) {
        mutableGames.value = snapshot.games
        mutableProfile.value = snapshot.profile
    }
}
