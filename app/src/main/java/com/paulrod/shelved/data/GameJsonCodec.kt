package com.paulrod.shelved.data

import com.paulrod.shelved.data.model.Game
import com.paulrod.shelved.data.model.GameNote
import com.paulrod.shelved.data.model.GameStatus
import org.json.JSONArray
import org.json.JSONObject

internal fun Game.toStoredJson() = JSONObject().apply {
    put("id", id); put("name", name); put("coverImageUrl", coverImageUrl)
    put("customCoverImagePath", customCoverImagePath)
    put("status", status.name); put("hoursPlayed", hoursPlayed)
    put("rating", rating); put("review", review)
    put("notes", JSONArray().apply { notes.forEach { put(it.toStoredJson()) } })
    put("released", released); put("playtime", playtime); put("description", description)
    put("platforms", JSONArray(platforms))
}

internal fun JSONObject.toStoredGame() = Game(
    id = getString("id"),
    name = getString("name"),
    coverImageUrl = optNullableString("coverImageUrl"),
    customCoverImagePath = optNullableString("customCoverImagePath"),
    status = runCatching { GameStatus.valueOf(optString("status")) }.getOrDefault(GameStatus.BACKLOG),
    hoursPlayed = if (has("hoursPlayed") && !isNull("hoursPlayed")) getInt("hoursPlayed") else null,
    rating = optInt("rating").takeIf { it in Game.MIN_RATING..Game.MAX_RATING },
    review = optString("review"),
    notes = readNotes(),
    released = optNullableString("released"),
    playtime = if (has("playtime") && !isNull("playtime")) getInt("playtime") else null,
    platforms = optJSONArray("platforms")?.let { array ->
        List(array.length()) { array.getString(it) }
    }.orEmpty(),
    description = optNullableString("description"),
)

private fun GameNote.toStoredJson() = JSONObject().apply {
    put("text", text)
    put("createdAtEpochMillis", createdAtEpochMillis)
}

private fun JSONObject.readNotes(): List<GameNote> {
    optJSONArray("notes")?.let { array ->
        return List(array.length()) { index ->
            array.getJSONObject(index).let { note ->
                GameNote(
                    text = note.getString("text"),
                    createdAtEpochMillis = note.optLong("createdAtEpochMillis"),
                )
            }
        }
    }

    return listOfNotNull(
        optNullableString("notes")?.let { GameNote(text = it, createdAtEpochMillis = 0L) },
    )
}

private fun JSONObject.optNullableString(key: String): String? =
    if (!has(key) || isNull(key)) null else optString(key).takeIf { it.isNotBlank() }
