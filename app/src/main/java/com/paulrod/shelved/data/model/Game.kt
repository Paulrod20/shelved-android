package com.paulrod.shelved.data.model

enum class GameStatus { BACKLOG, PLAYING, COMPLETED }

data class GameNote(
    val text: String,
    val createdAtEpochMillis: Long,
)

data class Game(
    val id: String,
    val name: String,
    val coverImageUrl: String? = null,
    val status: GameStatus = GameStatus.BACKLOG,
    val hoursPlayed: Int? = null,
    val notes: List<GameNote> = emptyList(),
    val released: String? = null,
    val playtime: Int? = null,
    val platforms: List<String> = emptyList(),
    val description: String? = null,
)
