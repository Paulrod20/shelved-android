package com.paulrod.shelved.ui.backlog

import com.paulrod.shelved.data.model.Game
import com.paulrod.shelved.data.model.GameStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class BacklogFilterTest {
    private val games = listOf(
        Game(id = "1", name = "Zelda", status = GameStatus.PLAYING),
        Game(id = "2", name = "Astro Bot", status = GameStatus.COMPLETED),
        Game(id = "3", name = "Control", status = GameStatus.PLAYING),
    )

    @Test
    fun recentlyAddedPreservesRepositoryOrder() {
        val result = filteredAndSortedGames(games, null, "", BacklogSort.RECENTLY_ADDED)

        assertEquals(listOf("Zelda", "Astro Bot", "Control"), result.map { it.name })
    }

    @Test
    fun statusAndQueryAreCombined() {
        val result = filteredAndSortedGames(games, GameStatus.PLAYING, "con", BacklogSort.RECENTLY_ADDED)

        assertEquals(listOf("Control"), result.map { it.name })
    }

    @Test
    fun alphabeticalSortIsCaseInsensitiveInBothDirections() {
        val ascending = filteredAndSortedGames(games, null, "", BacklogSort.NAME_ASCENDING)
        val descending = filteredAndSortedGames(games, null, "", BacklogSort.NAME_DESCENDING)

        assertEquals(listOf("Astro Bot", "Control", "Zelda"), ascending.map { it.name })
        assertEquals(listOf("Zelda", "Control", "Astro Bot"), descending.map { it.name })
    }

    @Test
    fun ratingSortWorksInBothDirectionsAndKeepsUnratedGamesLast() {
        val ratedGames = listOf(
            Game(id = "unrated", name = "Unrated"),
            Game(id = "five", name = "Five stars", rating = 5),
            Game(id = "one", name = "One star", rating = 1),
            Game(id = "three", name = "Three stars", rating = 3),
        )

        val highToLow = filteredAndSortedGames(ratedGames, null, "", BacklogSort.RATING_HIGH_TO_LOW)
        val lowToHigh = filteredAndSortedGames(ratedGames, null, "", BacklogSort.RATING_LOW_TO_HIGH)

        assertEquals(listOf(5, 3, 1, null), highToLow.map(Game::rating))
        assertEquals(listOf(1, 3, 5, null), lowToHigh.map(Game::rating))
    }
}
