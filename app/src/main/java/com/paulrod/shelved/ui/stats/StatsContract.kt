package com.paulrod.shelved.ui.stats

import com.paulrod.shelved.data.model.GameStatus

data class StatsUiState(
    val totalGames: Int = 0,
    val totalHours: Int = 0,
    val statusCounts: Map<GameStatus, Int> = GameStatus.entries.associateWith { 0 },
) {
    val isEmpty: Boolean get() = totalGames == 0
}
