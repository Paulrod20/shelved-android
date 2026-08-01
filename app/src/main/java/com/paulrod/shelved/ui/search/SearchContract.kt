package com.paulrod.shelved.ui.search

import com.paulrod.shelved.data.model.Game

data class SearchUiState(
    val query: String = "",
    val results: List<Game> = emptyList(),
    val status: SearchStatus = SearchStatus.Idle,
    val selectedGame: Game? = null,
    val libraryGameIds: Set<String> = emptySet(),
)

sealed interface SearchStatus {
    data object Idle : SearchStatus
    data object Loading : SearchStatus
    data object Ready : SearchStatus
    data class Error(val message: String) : SearchStatus
}

sealed interface SearchAction {
    data class QueryChanged(val query: String) : SearchAction
    data class GameSelected(val game: Game) : SearchAction
    data object GameDismissed : SearchAction
    data class GameAdded(val game: Game) : SearchAction
}
