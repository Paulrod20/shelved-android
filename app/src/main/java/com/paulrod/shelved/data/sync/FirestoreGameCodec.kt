package com.paulrod.shelved.data.sync

import com.paulrod.shelved.data.model.Game
import com.paulrod.shelved.data.model.GameNote
import com.paulrod.shelved.data.model.GameStatus

internal fun Game.toCloudMap(): Map<String, Any?> = mapOf(
    "name" to name,
    "coverImageUrl" to coverImageUrl,
    "status" to status.name,
    "hoursPlayed" to hoursPlayed,
    "rating" to rating,
    "review" to review,
    "notes" to notes.map { note ->
        mapOf("text" to note.text, "createdAtEpochMillis" to note.createdAtEpochMillis)
    },
    "released" to released,
    "playtime" to playtime,
    "platforms" to platforms,
    "description" to description,
)

internal fun Map<String, Any?>.toCloudGame(id: String): Game? {
    val name = string("name") ?: return null
    return Game(
        id = id,
        name = name,
        coverImageUrl = string("coverImageUrl"),
        status = string("status")?.let { stored ->
            runCatching { GameStatus.valueOf(stored) }.getOrNull()
        } ?: GameStatus.BACKLOG,
        hoursPlayed = number("hoursPlayed")?.toInt(),
        rating = number("rating")?.toInt()?.takeIf { it in Game.MIN_RATING..Game.MAX_RATING },
        review = string("review").orEmpty(),
        notes = mapList("notes").mapNotNull { note ->
            note.string("text")?.let { text ->
                GameNote(text, note.number("createdAtEpochMillis")?.toLong() ?: 0L)
            }
        },
        released = string("released"),
        playtime = number("playtime")?.toInt(),
        platforms = stringList("platforms"),
        description = string("description"),
    )
}

private fun Any?.asStringMap(): Map<String, Any?>? =
    (this as? Map<*, *>)?.entries?.associate { (key, value) -> key.toString() to value }

private fun Map<String, Any?>.string(key: String) = this[key] as? String
private fun Map<String, Any?>.number(key: String) = this[key] as? Number
private fun Map<String, Any?>.stringList(key: String) =
    (this[key] as? List<*>)?.mapNotNull { it as? String }.orEmpty()

private fun Map<String, Any?>.mapList(key: String) =
    (this[key] as? List<*>)?.mapNotNull { it.asStringMap() }.orEmpty()
