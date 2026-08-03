package com.paulrod.shelved.data

import com.paulrod.shelved.data.model.Game

interface GameCatalog {
    suspend fun search(query: String): List<Game>
    suspend fun details(game: Game): Game
}
