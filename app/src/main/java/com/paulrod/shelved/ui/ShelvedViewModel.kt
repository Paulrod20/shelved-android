package com.paulrod.shelved.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.paulrod.shelved.data.RawgApi
import com.paulrod.shelved.data.ShelvedRepository
import com.paulrod.shelved.data.model.Game
import com.paulrod.shelved.data.model.Profile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ShelvedViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ShelvedRepository(application)
    private val api = RawgApi()
    private var searchJob: Job? = null

    val games = repository.games
    val profile = repository.profile
    private val _searchResults = MutableStateFlow<List<Game>>(emptyList())
    private val _searchState = MutableStateFlow<SearchState>(SearchState.Idle)
    val searchResults: StateFlow<List<Game>> = _searchResults
    val searchState: StateFlow<SearchState> = _searchState

    fun addGame(game: Game) = repository.addGame(game)
    fun updateGame(game: Game) = repository.updateGame(game)
    fun updateProfile(profile: Profile) = repository.updateProfile(profile)

    fun search(query: String) {
        searchJob?.cancel()
        if (query.isBlank()) {
            _searchResults.value = emptyList(); _searchState.value = SearchState.Idle; return
        }
        searchJob = viewModelScope.launch {
            delay(300)
            _searchState.value = SearchState.Loading
            runCatching { withContext(Dispatchers.IO) { api.search(query) } }
                .onSuccess { _searchResults.value = it; _searchState.value = SearchState.Ready }
                .onFailure { _searchResults.value = emptyList(); _searchState.value = SearchState.Error(it.message ?: "Search failed.") }
        }
    }

    suspend fun loadDetails(game: Game): Game = withContext(Dispatchers.IO) { api.details(game) }
}

sealed interface SearchState {
    data object Idle : SearchState
    data object Loading : SearchState
    data object Ready : SearchState
    data class Error(val message: String) : SearchState
}
