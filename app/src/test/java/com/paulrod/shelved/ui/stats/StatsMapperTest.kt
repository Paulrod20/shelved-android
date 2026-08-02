package com.paulrod.shelved.ui.stats

import com.paulrod.shelved.data.model.Game
import com.paulrod.shelved.data.model.GameStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StatsMapperTest {
    @Test
    fun emptyLibraryProducesEmptyStats() {
        val state = buildStatsUiState(emptyList())

        assertEquals(0, state.totalGames)
        assertEquals(0, state.totalHours)
        assertEquals(0, state.completionRate)
        assertEquals(0f, state.averageHoursPerTrackedGame)
        assertNull(state.topPlatform)
        assertEquals(emptyList<Game>(), state.mostPlayedGames)
    }

    @Test
    fun derivesSummaryProgressAndPlaytimeMetrics() {
        val games = listOf(
            game("1", "Halo", GameStatus.COMPLETED, 40, "Xbox", "PC"),
            game("2", "Forza", GameStatus.PLAYING, 20, "Xbox"),
            game("3", "Celeste", GameStatus.COMPLETED, 10, "PC"),
            game("4", "Unplayed", GameStatus.BACKLOG, null, "Switch"),
            game("5", "Zero", GameStatus.BACKLOG, 0, "Xbox"),
        )

        val state = buildStatsUiState(games)

        assertEquals(5, state.totalGames)
        assertEquals(70, state.totalHours)
        assertEquals(2, state.completedGames)
        assertEquals(40, state.completionRate)
        assertEquals(3, state.trackedGames)
        assertEquals(70f / 3f, state.averageHoursPerTrackedGame)
        assertEquals("Xbox", state.topPlatform)
        assertEquals(listOf("Halo", "Forza", "Celeste"), state.mostPlayedGames.map { it.name })
    }

    @Test
    fun platformAndPlaytimeTiesUseAlphabeticalOrder() {
        val games = listOf(
            game("1", "Zelda", GameStatus.PLAYING, 12, "Switch"),
            game("2", "Astro Bot", GameStatus.PLAYING, 12, "PlayStation"),
        )

        val state = buildStatsUiState(games)

        assertEquals("PlayStation", state.topPlatform)
        assertEquals(listOf("Astro Bot", "Zelda"), state.mostPlayedGames.map { it.name })
    }

    private fun game(
        id: String,
        name: String,
        status: GameStatus,
        hours: Int?,
        vararg platforms: String,
    ) = Game(
        id = id,
        name = name,
        status = status,
        hoursPlayed = hours,
        platforms = platforms.toList(),
    )
}
