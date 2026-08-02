package com.paulrod.shelved.data

import com.paulrod.shelved.data.model.Game
import kotlinx.coroutines.flow.StateFlow

interface ShelvedDataRepository {
    val games: StateFlow<List<Game>>

    fun addGame(game: Game)
    fun updateGame(game: Game)
    fun deleteGames(gameIds: Set<String>)
}
