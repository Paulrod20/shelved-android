package com.paulrod.shelved.data.sync

import com.paulrod.shelved.data.model.Game
import com.paulrod.shelved.data.model.Platform
import com.paulrod.shelved.data.model.Profile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LibraryMergeTest {
    @Test
    fun firstSignInKeepsLocalGamesAndRestoresMissingCloudGames() {
        val local = LibrarySnapshot(
            games = listOf(game("shared", "Local version"), game("local", "Local only")),
            profile = Profile(displayName = "Local player", favoriteGameIds = listOf("local")),
        )
        val cloud = LibrarySnapshot(
            games = listOf(game("shared", "Cloud version"), game("cloud", "Cloud only")),
            profile = Profile(
                displayName = "Cloud player",
                bio = "Cloud bio",
                favoritePlatforms = listOf(Platform.PC),
                favoriteGameIds = listOf("cloud", "missing"),
            ),
        )

        val merged = LibraryMerge.merge(local, cloud)

        assertEquals(listOf("shared", "local", "cloud"), merged.games.map(Game::id))
        assertEquals("Local version", merged.games.first().name)
        assertEquals("Local player", merged.profile.displayName)
        assertEquals("Cloud bio", merged.profile.bio)
        assertEquals(listOf(Platform.PC), merged.profile.favoritePlatforms)
        assertEquals(listOf("local", "cloud"), merged.profile.favoriteGameIds)
    }

    @Test
    fun deviceOnlyImagePathsNeverEnterCloudChanges() {
        val snapshot = LibrarySnapshot(
            games = listOf(game("game", "Game").copy(customCoverImagePath = "/local/cover.jpg")),
            profile = Profile(profileImagePath = "/local/avatar.jpg"),
        )

        val changes = snapshot.changesSince(previous = null)

        assertNull(changes.gamesToUpsert.single().customCoverImagePath)
        assertNull(changes.profile?.profileImagePath)
    }

    @Test
    fun changingShelfOrderCreatesAnOrderUpdateWithoutRewritingGames() {
        val first = game("first", "First")
        val second = game("second", "Second")
        val previous = LibrarySnapshot(games = listOf(first, second))

        val changes = LibrarySnapshot(games = listOf(second, first)).changesSince(previous)

        assertEquals(listOf("second", "first"), changes.gameOrder)
        assertTrue(changes.gamesToUpsert.isEmpty())
    }

    @Test
    fun changingRatingOrReviewCreatesACloudGameUpdate() {
        val previousGame = game("game", "Game")
        val reviewedGame = previousGame.copy(rating = 5, review = "A favorite.")

        val changes = LibrarySnapshot(games = listOf(reviewedGame))
            .changesSince(LibrarySnapshot(games = listOf(previousGame)))

        assertEquals(listOf(reviewedGame), changes.gamesToUpsert)
    }

    private fun game(id: String, name: String) = Game(id = id, name = name)
}
