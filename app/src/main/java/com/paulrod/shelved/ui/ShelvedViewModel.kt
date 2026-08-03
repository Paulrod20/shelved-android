package com.paulrod.shelved.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulrod.shelved.data.CloudflareGameCatalog
import com.paulrod.shelved.data.GameCatalog
import com.paulrod.shelved.data.ShelvedDataRepository
import com.paulrod.shelved.data.model.Game
import com.paulrod.shelved.ui.search.SearchAction
import com.paulrod.shelved.ui.search.SearchStatus
import com.paulrod.shelved.ui.search.SearchUiState
import com.paulrod.shelved.ui.search.toSearchFailure
import com.paulrod.shelved.ui.stats.StatsUiState
import com.paulrod.shelved.ui.stats.buildStatsUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

class ShelvedViewModel(
    private val repository: ShelvedDataRepository,
    private val catalog: GameCatalog = CloudflareGameCatalog(),
    private val catalogDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private val _searchUiState = MutableStateFlow(SearchUiState())
    private var searchJob: Job? = null
    private var detailsJob: Job? = null

    val statsUiState: StateFlow<StatsUiState> = repository.games
        .map(::buildStatsUiState)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatsUiState())

    val searchUiState: StateFlow<SearchUiState> = combine(_searchUiState, repository.games) { state, games ->
        state.copy(libraryGameIds = games.mapTo(mutableSetOf()) { it.id })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SearchUiState())

    fun onSearchAction(action: SearchAction) {
        when (action) {
            is SearchAction.QueryChanged -> {
                _searchUiState.value = _searchUiState.value.copy(query = action.query)
                searchJob?.cancel()
                searchJob = launchSearch(action.query)
            }
            SearchAction.RetryRequested -> {
                searchJob?.cancel()
                searchJob = launchSearch(_searchUiState.value.query, debounce = false)
            }
            is SearchAction.GameSelected -> loadGameDetails(action.game)
            SearchAction.GameDismissed -> {
                detailsJob?.cancel()
                _searchUiState.value = _searchUiState.value.copy(
                    selectedGame = null,
                    isDetailsLoading = false,
                    detailsError = null,
                )
            }
            is SearchAction.GameAdded -> {
                repository.addGame(action.game)
                _searchUiState.value = _searchUiState.value.copy(selectedGame = null, detailsError = null)
            }
        }
    }

    private fun launchSearch(query: String, debounce: Boolean = true): Job? {
        if (query.isBlank()) {
            _searchUiState.value = SearchUiState()
            return null
        }
        return viewModelScope.launch {
            if (debounce) delay(300.milliseconds)
            _searchUiState.value = _searchUiState.value.copy(status = SearchStatus.Loading)
            try {
                val results = withContext(catalogDispatcher) { catalog.search(query) }
                _searchUiState.value = _searchUiState.value.copy(
                    results = results,
                    status = SearchStatus.Ready,
                )
            } catch (error: Throwable) {
                if (error is CancellationException) throw error
                _searchUiState.value = _searchUiState.value.copy(
                    results = emptyList(),
                    status = SearchStatus.Error(error.toSearchFailure()),
                )
            }
        }
    }

    private fun loadGameDetails(game: Game) {
        detailsJob?.cancel()
        _searchUiState.value = _searchUiState.value.copy(
            selectedGame = game,
            isDetailsLoading = true,
            detailsError = null,
        )
        detailsJob = viewModelScope.launch {
            try {
                val details = withContext(catalogDispatcher) { catalog.details(game) }
                _searchUiState.value = _searchUiState.value.copy(
                    selectedGame = game.mergeDetails(details),
                    isDetailsLoading = false,
                )
            } catch (error: Throwable) {
                if (error is CancellationException) throw error
                _searchUiState.value = _searchUiState.value.copy(
                    isDetailsLoading = false,
                    detailsError = error.message ?: "Could not load all game details.",
                )
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
