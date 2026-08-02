package com.paulrod.shelved.ui.stats

import com.paulrod.shelved.data.model.Game
import com.paulrod.shelved.data.model.GameStatus
import kotlin.math.roundToInt

data class StatsUiState(
    val totalGames: Int = 0,
    val totalHours: Int = 0,
    val statusCounts: Map<GameStatus, Int> = GameStatus.entries.associateWith { 0 },
    val trackedGames: Int = 0,
    val topPlatform: String? = null,
    val mostPlayedGames: List<Game> = emptyList(),
) {
    val isEmpty: Boolean get() = totalGames == 0
    val completedGames: Int get() = statusCounts[GameStatus.COMPLETED] ?: 0
    val completionRate: Int
        get() = if (totalGames == 0) 0 else (completedGames * 100f / totalGames).roundToInt()
    val averageHoursPerTrackedGame: Float
        get() = if (trackedGames == 0) 0f else totalHours.toFloat() / trackedGames
}
