package com.paulrod.shelved.data.model

enum class GameStatus { BACKLOG, PLAYING, COMPLETED }

data class Game(
    val id: String,
    val name: String,
    val coverColor: String = "#2a2a30",
    val coverImageUrl: String? = null,
    val status: GameStatus = GameStatus.BACKLOG,
    val hoursPlayed: Int? = null,
    val notes: String? = null,
)
