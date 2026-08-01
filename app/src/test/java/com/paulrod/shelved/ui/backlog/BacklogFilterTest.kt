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
}
