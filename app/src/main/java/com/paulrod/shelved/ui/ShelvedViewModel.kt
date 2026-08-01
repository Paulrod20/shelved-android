package com.paulrod.shelved.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.paulrod.shelved.data.RawgApi
import com.paulrod.shelved.data.ShelvedRepository
import com.paulrod.shelved.data.model.Game
import com.paulrod.shelved.data.model.GameStatus
import com.paulrod.shelved.ui.backlog.BacklogAction
import com.paulrod.shelved.ui.backlog.BacklogSort
import com.paulrod.shelved.ui.backlog.BacklogUiState
import com.paulrod.shelved.ui.backlog.filteredAndSortedGames
import com.paulrod.shelved.ui.profile.ProfileAction
import com.paulrod.shelved.ui.profile.ProfileUiState
import com.paulrod.shelved.ui.search.SearchAction
import com.paulrod.shelved.ui.search.SearchStatus
import com.paulrod.shelved.ui.search.SearchUiState
import com.paulrod.shelved.ui.stats.StatsUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
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

class ShelvedViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ShelvedRepository(application)
    private val api = RawgApi()
    private val backlogControls = MutableStateFlow(BacklogControls())
    private val profileControls = MutableStateFlow(ProfileControls())
    private val _searchUiState = MutableStateFlow(SearchUiState())
    private val _addSearchUiState = MutableStateFlow(SearchUiState())
    private var searchJob: Job? = null
    private var addSearchJob: Job? = null
    private var detailsJob: Job? = null

    val backlogUiState: StateFlow<BacklogUiState> = combine(repository.games, backlogControls) { games, controls ->
        BacklogUiState(
            allGames = games,
            visibleGames = filteredAndSortedGames(games, controls.statusFilter, controls.searchQuery, controls.sortOrder),
            statusFilter = controls.statusFilter,
            searchQuery = controls.searchQuery,
            isSearchVisible = controls.isSearchVisible,
            sortOrder = controls.sortOrder,
            isSortSheetVisible = controls.isSortSheetVisible,
            isAddSheetVisible = controls.isAddSheetVisible,
            selectedGame = controls.selectedGame,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BacklogUiState())

    val profileUiState: StateFlow<ProfileUiState> = combine(
        repository.profile,
        repository.games,
        profileControls,
    ) { profile, games, controls ->
        ProfileUiState(
            profile = profile,
            games = games,
            favoriteGames = profile.favoriteGameIds.mapNotNull { id -> games.find { it.id == id } },
            isEditSheetVisible = controls.isEditSheetVisible,
            isMenuSheetVisible = controls.isMenuSheetVisible,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())

    val statsUiState: StateFlow<StatsUiState> = repository.games.map { games ->
        StatsUiState(
            totalGames = games.size,
            totalHours = games.sumOf { it.hoursPlayed ?: 0 },
            statusCounts = GameStatus.entries.associateWith { status -> games.count { it.status == status } },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatsUiState())

    val searchUiState: StateFlow<SearchUiState> = combine(_searchUiState, repository.games) { state, games ->
        state.copy(libraryGameIds = games.mapTo(mutableSetOf()) { it.id })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SearchUiState())
    val addSearchUiState: StateFlow<SearchUiState> = _addSearchUiState

    fun onBacklogAction(action: BacklogAction) {
        when (action) {
            BacklogAction.ToggleSearch -> backlogControls.update { controls ->
                val visible = !controls.isSearchVisible
                controls.copy(isSearchVisible = visible, searchQuery = if (visible) controls.searchQuery else "")
            }
            is BacklogAction.SearchChanged -> backlogControls.update { it.copy(searchQuery = action.query) }
            is BacklogAction.StatusSelected -> backlogControls.update { it.copy(statusFilter = action.status) }
            BacklogAction.SortRequested -> backlogControls.update { it.copy(isSortSheetVisible = true) }
            BacklogAction.SortDismissed -> backlogControls.update { it.copy(isSortSheetVisible = false) }
            is BacklogAction.SortSelected -> backlogControls.update {
                it.copy(sortOrder = action.sort, isSortSheetVisible = false)
            }
            BacklogAction.AddRequested -> {
                resetAddSearch()
                backlogControls.update { it.copy(isAddSheetVisible = true) }
            }
            BacklogAction.AddDismissed -> backlogControls.update { it.copy(isAddSheetVisible = false) }
            is BacklogAction.GameSelected -> backlogControls.update { it.copy(selectedGame = action.game) }
            BacklogAction.GameDismissed -> backlogControls.update { it.copy(selectedGame = null) }
            is BacklogAction.GameAdded -> {
                repository.addGame(action.game)
                backlogControls.update { it.copy(isAddSheetVisible = false) }
                resetAddSearch()
            }
            is BacklogAction.GameSaved -> {
                repository.updateGame(action.game)
                backlogControls.update { it.copy(selectedGame = null) }
            }
        }
    }

    fun onSearchAction(action: SearchAction) {
        when (action) {
            is SearchAction.QueryChanged -> {
                _searchUiState.value = _searchUiState.value.copy(query = action.query)
                searchJob?.cancel()
                searchJob = launchSearch(action.query, _searchUiState)
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

    fun onAddSearchQueryChanged(query: String) {
        _addSearchUiState.value = _addSearchUiState.value.copy(query = query)
        addSearchJob?.cancel()
        addSearchJob = launchSearch(query, _addSearchUiState)
    }

    fun onAddSearchGameSelected(game: Game?) {
        _addSearchUiState.value = _addSearchUiState.value.copy(selectedGame = game)
    }

    fun onProfileAction(action: ProfileAction) {
        when (action) {
            ProfileAction.EditRequested -> profileControls.update { it.copy(isEditSheetVisible = true) }
            ProfileAction.EditDismissed -> profileControls.update { it.copy(isEditSheetVisible = false) }
            ProfileAction.MenuRequested -> profileControls.update { it.copy(isMenuSheetVisible = true) }
            ProfileAction.MenuDismissed -> profileControls.update { it.copy(isMenuSheetVisible = false) }
            is ProfileAction.ProfileSaved -> {
                repository.updateProfile(action.profile)
                profileControls.update { it.copy(isEditSheetVisible = false) }
            }
        }
    }

    private fun launchSearch(query: String, destination: MutableStateFlow<SearchUiState>): Job? {
        if (query.isBlank()) {
            destination.value = SearchUiState()
            return null
        }
        return viewModelScope.launch {
            delay(300.milliseconds)
            destination.value = destination.value.copy(status = SearchStatus.Loading)
            runCatching { withContext(Dispatchers.IO) { api.search(query) } }
                .onSuccess { results -> destination.value = destination.value.copy(results = results, status = SearchStatus.Ready) }
                .onFailure { error ->
                    destination.value = destination.value.copy(
                        results = emptyList(),
                        status = SearchStatus.Error(error.message ?: "Search failed."),
                    )
                }
        }
    }

    private fun resetAddSearch() {
        addSearchJob?.cancel()
        _addSearchUiState.value = SearchUiState()
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
                val details = withContext(Dispatchers.IO) { api.details(game) }
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

private data class BacklogControls(
    val statusFilter: GameStatus? = null,
    val searchQuery: String = "",
    val isSearchVisible: Boolean = false,
    val sortOrder: BacklogSort = BacklogSort.RECENTLY_ADDED,
    val isSortSheetVisible: Boolean = false,
    val isAddSheetVisible: Boolean = false,
    val selectedGame: Game? = null,
)

private data class ProfileControls(
    val isEditSheetVisible: Boolean = false,
    val isMenuSheetVisible: Boolean = false,
)

private inline fun <T> MutableStateFlow<T>.update(transform: (T) -> T) {
    value = transform(value)
}
