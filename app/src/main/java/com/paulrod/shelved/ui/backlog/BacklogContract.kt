package com.paulrod.shelved.ui.backlog

import com.paulrod.shelved.data.model.Game
import com.paulrod.shelved.data.model.GameStatus
import com.paulrod.shelved.data.cover.CoverCropRequest

enum class BacklogSort(val label: String) {
    RECENTLY_ADDED("Recently added"),
    NAME_ASCENDING("Name A–Z"),
    NAME_DESCENDING("Name Z–A"),
}

data class BacklogUiState(
    val allGames: List<Game> = emptyList(),
    val visibleGames: List<Game> = emptyList(),
    val statusFilter: GameStatus? = null,
    val searchQuery: String = "",
    val isSearchVisible: Boolean = false,
    val sortOrder: BacklogSort = BacklogSort.RECENTLY_ADDED,
    val isSortSheetVisible: Boolean = false,
    val isAddSheetVisible: Boolean = false,
    val selectedGame: Game? = null,
    val selectedGameIds: Set<String> = emptySet(),
    val isDeleteConfirmationVisible: Boolean = false,
    val isCoverImageLoading: Boolean = false,
    val hasCoverImageError: Boolean = false,
)

sealed interface BacklogAction {
    data object ToggleSearch : BacklogAction
    data class SearchChanged(val query: String) : BacklogAction
    data class StatusSelected(val status: GameStatus?) : BacklogAction
    data object SortRequested : BacklogAction
    data object SortDismissed : BacklogAction
    data class SortSelected(val sort: BacklogSort) : BacklogAction
    data object AddRequested : BacklogAction
    data object AddDismissed : BacklogAction
    data class GameSelected(val game: Game) : BacklogAction
    data object GameDismissed : BacklogAction
    data class GameAdded(val game: Game) : BacklogAction
    data class GameSaved(val game: Game) : BacklogAction
    data class CoverCropConfirmed(val request: CoverCropRequest) : BacklogAction
    data object CustomCoverRemoved : BacklogAction
    data class GameLongPressed(val gameId: String) : BacklogAction
    data class GameSelectionToggled(val gameId: String) : BacklogAction
    data object SelectionCleared : BacklogAction
    data object DeleteRequested : BacklogAction
    data object DeleteDismissed : BacklogAction
    data object DeleteConfirmed : BacklogAction
}

internal fun filteredAndSortedGames(
    games: List<Game>,
    status: GameStatus?,
    query: String,
    sort: BacklogSort,
): List<Game> {
    val matchingGames = games.filter { game ->
        (status == null || game.status == status) &&
            (query.isBlank() || game.name.contains(query.trim(), ignoreCase = true))
    }
    return when (sort) {
        BacklogSort.RECENTLY_ADDED -> matchingGames
        BacklogSort.NAME_ASCENDING -> matchingGames.sortedBy { it.name.lowercase() }
        BacklogSort.NAME_DESCENDING -> matchingGames.sortedByDescending { it.name.lowercase() }
    }
}
