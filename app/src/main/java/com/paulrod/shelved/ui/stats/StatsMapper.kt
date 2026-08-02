package com.paulrod.shelved.ui.stats

import com.paulrod.shelved.data.model.Game
import com.paulrod.shelved.data.model.GameStatus

internal fun buildStatsUiState(games: List<Game>): StatsUiState {
    val gamesWithPlaytime = games.filter { (it.hoursPlayed ?: 0) > 0 }
    val statusCounts = GameStatus.entries.associateWith { status -> games.count { it.status == status } }
    val platformCounts = games
        .flatMap { game -> game.platforms.filter(String::isNotBlank).distinct() }
        .groupingBy(String::trim)
        .eachCount()

    return StatsUiState(
        totalGames = games.size,
        totalHours = games.sumOf { (it.hoursPlayed ?: 0).coerceAtLeast(0) },
        statusCounts = statusCounts,
        trackedGames = gamesWithPlaytime.size,
        topPlatform = platformCounts.entries
            .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key.lowercase() })
            .firstOrNull()
            ?.key,
        mostPlayedGames = gamesWithPlaytime
            .sortedWith(compareByDescending<Game> { it.hoursPlayed ?: 0 }.thenBy { it.name.lowercase() })
            .take(3),
    )
}
