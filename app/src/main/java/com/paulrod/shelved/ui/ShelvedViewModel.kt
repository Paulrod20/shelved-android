package com.paulrod.shelved.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulrod.shelved.data.GameCatalog
import com.paulrod.shelved.data.ShelvedDataRepository
import com.paulrod.shelved.data.model.Game
import com.paulrod.shelved.ui.search.GameSearchController
import com.paulrod.shelved.ui.search.SearchAction
import com.paulrod.shelved.ui.search.SearchUiState
import com.paulrod.shelved.ui.stats.StatsUiState
import com.paulrod.shelved.ui.stats.buildStatsUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ShelvedViewModel(
    private val repository: ShelvedDataRepository,
    private val catalog: GameCatalog,
) : ViewModel() {
    private val search = GameSearchController(viewModelScope, catalog)
    private var detailsJob: Job? = null

    val statsUiState: StateFlow<StatsUiState> = repository.games
        .map(::buildStatsUiState)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatsUiState())

    val searchUiState: StateFlow<SearchUiState> = combine(search.state, repository.games) { state, games ->
        state.copy(libraryGameIds = games.mapTo(mutableSetOf()) { it.id })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SearchUiState())

    fun onSearchAction(action: SearchAction) {
        when (action) {
            is SearchAction.QueryChanged -> {
                search.queryChanged(action.query)
            }
            SearchAction.RetryRequested -> search.retry()
            is SearchAction.GameSelected -> loadGameDetails(action.game)
            SearchAction.GameDismissed -> {
                detailsJob?.cancel()
                search.update {
                    it.copy(
                        selectedGame = null,
                        isDetailsLoading = false,
                        detailsError = null,
                    )
                }
            }
            is SearchAction.GameAdded -> {
                repository.addGame(action.game)
                search.update { it.copy(selectedGame = null, detailsError = null) }
            }
        }
    }

    private fun loadGameDetails(game: Game) {
        detailsJob?.cancel()
        search.update { state ->
            state.copy(
                selectedGame = game,
                isDetailsLoading = true,
                detailsError = null,
            )
        }
        detailsJob = viewModelScope.launch {
            try {
                val details = catalog.details(game)
                search.update { state ->
                    state.copy(
                        selectedGame = game.mergeDetails(details),
                        isDetailsLoading = false,
                    )
                }
            } catch (error: Throwable) {
                if (error is CancellationException) throw error
                search.update { state ->
                    state.copy(
                        isDetailsLoading = false,
                        detailsError = error.message ?: "Could not load all game details.",
                    )
                }
            }
        }
    }
}

private fun Game.mergeDetails(details: Game): Game = copy(
    coverImageUrl = details.coverImageUrl ?: coverImageUrl,
    released = details.released ?: released,
    playtime = details.playtime ?: playtime,
    platforms = details.platforms.ifEmpty { platforms },
    description = details.description,
)
