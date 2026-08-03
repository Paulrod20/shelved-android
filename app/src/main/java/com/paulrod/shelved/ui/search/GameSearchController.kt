package com.paulrod.shelved.ui.search

import com.paulrod.shelved.data.GameCatalog
import com.paulrod.shelved.data.model.Game
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/** Owns the shared query, debounce, retry, and result lifecycle for game search. */
internal class GameSearchController(
    private val scope: CoroutineScope,
    private val catalog: GameCatalog,
    private val debounceDuration: Duration = 300.milliseconds,
) {
    private val mutableState = MutableStateFlow(SearchUiState())
    private var searchJob: Job? = null

    val state: StateFlow<SearchUiState> = mutableState

    fun queryChanged(query: String) {
        update { it.copy(query = query) }
        launch(query, debounce = true)
    }

    fun retry() = launch(mutableState.value.query, debounce = false)

    fun reset() {
        searchJob?.cancel()
        searchJob = null
        mutableState.value = SearchUiState()
    }

    fun selectGame(game: Game?) = update { it.copy(selectedGame = game) }

    fun update(transform: (SearchUiState) -> SearchUiState) {
        mutableState.value = transform(mutableState.value)
    }

    private fun launch(query: String, debounce: Boolean) {
        searchJob?.cancel()
        if (query.isBlank()) {
            mutableState.value = SearchUiState()
            searchJob = null
            return
        }
        searchJob = scope.launch {
            if (debounce) delay(debounceDuration)
            update { it.copy(status = SearchStatus.Loading) }
            try {
                val results = catalog.search(query)
                update { it.copy(results = results, status = SearchStatus.Ready) }
            } catch (error: Throwable) {
                if (error is CancellationException) throw error
                update {
                    it.copy(
                        results = emptyList(),
                        status = SearchStatus.Error(error.toSearchFailure()),
                    )
                }
            }
        }
    }
}
