package com.paulrod.shelved.data.sync

import com.paulrod.shelved.data.model.Game
import com.paulrod.shelved.data.model.GameNote
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class FirestoreGameCodecTest {
    @Test
    fun ratingAndPrivateReviewSurviveCloudRoundTrip() {
        val game = Game(
            id = "game",
            name = "Game",
            rating = 5,
            review = "My private review.",
            notes = listOf(GameNote("Private note", 123L)),
        )

        val cloudMap = game.toCloudMap()

        assertEquals(game, cloudMap.toCloudGame(game.id))
        assertFalse(cloudMap.containsKey("customCoverImagePath"))
    }

    @Test
    fun olderAndInvalidCloudRatingsLoadAsUnrated() {
        assertNull(mapOf<String, Any?>("name" to "Legacy").toCloudGame("legacy")?.rating)
        assertNull(
            mapOf<String, Any?>("name" to "Invalid", "rating" to 10L)
                .toCloudGame("invalid")
                ?.rating,
        )
    }
}
