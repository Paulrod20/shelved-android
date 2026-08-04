package com.paulrod.shelved.data

import com.paulrod.shelved.data.model.Game
import com.paulrod.shelved.data.model.GameNote
import com.paulrod.shelved.data.model.GameStatus
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GameJsonCodecTest {
    @Test
    fun ratingAndPrivateReviewSurviveDeviceStorageRoundTrip() {
        val game = Game(
            id = "game",
            name = "Game",
            coverImageUrl = "https://example.com/cover.jpg",
            customCoverImagePath = "/device/cover.jpg",
            status = GameStatus.COMPLETED,
            hoursPlayed = 24,
            rating = 4,
            review = "A thoughtful private review.",
            notes = listOf(GameNote("Private note", 123L)),
            released = "2026",
            playtime = 20,
            platforms = listOf("PC"),
            description = "Description",
        )

        assertEquals(game, game.toStoredJson().toStoredGame())
    }

    @Test
    fun olderStoredGamesDefaultToNoRatingOrReview() {
        val game = JSONObject()
            .put("id", "legacy")
            .put("name", "Legacy game")
            .toStoredGame()

        assertNull(game.rating)
        assertEquals("", game.review)
    }
}
