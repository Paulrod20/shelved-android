package com.paulrod.shelved.ui.backlog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulrod.shelved.data.RawgApi
import com.paulrod.shelved.data.ShelvedDataRepository
import com.paulrod.shelved.data.cover.CoverCropRequest
import com.paulrod.shelved.data.cover.GameCoverImageStorage
import com.paulrod.shelved.data.model.Game
import com.paulrod.shelved.data.model.GameStatus
import com.paulrod.shelved.ui.search.SearchStatus
import com.paulrod.shelved.ui.search.SearchUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

class BacklogViewModel(
    private val repository: ShelvedDataRepository,
    private val coverImageStorage: GameCoverImageStorage,
    private val api: RawgApi = RawgApi(),
) : ViewModel() {
    private val controls = MutableStateFlow(BacklogControls())
    private val _addSearchUiState = MutableStateFlow(SearchUiState())
    private var addSearchJob: Job? = null
    private var gameEditorSession = 0L

    val uiState: StateFlow<BacklogUiState> = combine(repository.games, controls) { games, controls ->
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
            selectedGameIds = controls.selectedGameIds,
            isDeleteConfirmationVisible = controls.isDeleteConfirmationVisible,
            isCoverImageLoading = controls.isCoverImageLoading,
            hasCoverImageError = controls.hasCoverImageError,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BacklogUiState())

    val addSearchUiState: StateFlow<SearchUiState> = _addSearchUiState

    fun onAction(action: BacklogAction) {
        when (action) {
            BacklogAction.ToggleSearch -> controls.update {
                val visible = !it.isSearchVisible
                it.copy(isSearchVisible = visible, searchQuery = if (visible) it.searchQuery else "")
            }
            is BacklogAction.SearchChanged -> controls.update { it.copy(searchQuery = action.query) }
            is BacklogAction.StatusSelected -> controls.update { it.copy(statusFilter = action.status) }
            BacklogAction.SortRequested -> controls.update { it.copy(isSortSheetVisible = true) }
            BacklogAction.SortDismissed -> controls.update { it.copy(isSortSheetVisible = false) }
            is BacklogAction.SortSelected -> controls.update {
                it.copy(sortOrder = action.sort, isSortSheetVisible = false)
            }
            BacklogAction.AddRequested -> {
                resetAddSearch()
                controls.update { it.copy(isAddSheetVisible = true) }
            }
            BacklogAction.AddDismissed -> controls.update { it.copy(isAddSheetVisible = false) }
            is BacklogAction.GameSelected -> {
                gameEditorSession += 1
                controls.update { it.copy(selectedGame = action.game, hasCoverImageError = false) }
            }
            BacklogAction.GameDismissed -> dismissGameEditor()
            is BacklogAction.GameAdded -> {
                repository.addGame(action.game)
                controls.update { it.copy(isAddSheetVisible = false) }
                resetAddSearch()
            }
            is BacklogAction.GameSaved -> saveGame(action.game)
            is BacklogAction.CoverCropConfirmed -> updateGameCover(action.request)
            BacklogAction.CustomCoverRemoved -> removeCustomGameCover()
            is BacklogAction.GameLongPressed -> controls.update {
                it.copy(selectedGameIds = it.selectedGameIds + action.gameId)
            }
            is BacklogAction.GameSelectionToggled -> controls.update {
                val selectedIds = if (action.gameId in it.selectedGameIds) {
                    it.selectedGameIds - action.gameId
                } else {
                    it.selectedGameIds + action.gameId
                }
                it.copy(selectedGameIds = selectedIds)
            }
            BacklogAction.SelectionCleared -> controls.update {
                it.copy(selectedGameIds = emptySet(), isDeleteConfirmationVisible = false)
            }
            BacklogAction.DeleteRequested -> controls.update {
                if (it.selectedGameIds.isEmpty()) it else it.copy(isDeleteConfirmationVisible = true)
            }
            BacklogAction.DeleteDismissed -> controls.update { it.copy(isDeleteConfirmationVisible = false) }
            BacklogAction.DeleteConfirmed -> deleteSelectedGames()
        }
    }

    fun onAddSearchQueryChanged(query: String) {
        _addSearchUiState.value = _addSearchUiState.value.copy(query = query)
        addSearchJob?.cancel()
        addSearchJob = launchAddSearch(query)
    }

    fun onAddSearchGameSelected(game: Game?) {
        _addSearchUiState.value = _addSearchUiState.value.copy(selectedGame = game)
    }

    private fun saveGame(game: Game) {
        gameEditorSession += 1
        val previousCover = savedGame(game.id)?.customCoverImagePath
        repository.updateGame(game)
        controls.update {
            it.copy(
                selectedGame = null,
                isCoverImageLoading = false,
                hasCoverImageError = false,
            )
        }
        if (previousCover != game.customCoverImagePath) {
            viewModelScope.launch { coverImageStorage.remove(previousCover) }
        }
    }

    private fun updateGameCover(request: CoverCropRequest) {
        if (controls.value.isCoverImageLoading) return
        val session = gameEditorSession
        controls.update { it.copy(isCoverImageLoading = true, hasCoverImageError = false) }

        viewModelScope.launch {
            try {
                useSavedGameCover(coverImageStorage.save(request), session)
            } catch (error: Throwable) {
                if (error is CancellationException) throw error
                if (session == gameEditorSession) {
                    controls.update { it.copy(isCoverImageLoading = false, hasCoverImageError = true) }
                }
            }
        }
    }

    private suspend fun useSavedGameCover(path: String, session: Long) {
        val editingGame = controls.value.selectedGame
        if (editingGame == null || session != gameEditorSession) {
            coverImageStorage.remove(path)
            return
        }

        val committedPath = savedGame(editingGame.id)?.customCoverImagePath
        val previousDraft = editingGame.customCoverImagePath
        if (previousDraft != committedPath) coverImageStorage.remove(previousDraft)
        controls.update {
            it.copy(
                selectedGame = editingGame.copy(customCoverImagePath = path),
                isCoverImageLoading = false,
            )
        }
    }

    private fun removeCustomGameCover() {
        if (controls.value.isCoverImageLoading) return
        val editingGame = controls.value.selectedGame ?: return
        val session = gameEditorSession
        val draftPath = editingGame.customCoverImagePath
        val committedPath = savedGame(editingGame.id)?.customCoverImagePath
        controls.update { it.copy(isCoverImageLoading = true, hasCoverImageError = false) }

        viewModelScope.launch {
            try {
                if (draftPath != committedPath) coverImageStorage.remove(draftPath)
                if (session == gameEditorSession) {
                    controls.update {
                        it.copy(
                            selectedGame = editingGame.copy(customCoverImagePath = null),
                            isCoverImageLoading = false,
                        )
                    }
                }
            } catch (error: Throwable) {
                if (error is CancellationException) throw error
                if (session == gameEditorSession) {
                    controls.update { it.copy(isCoverImageLoading = false, hasCoverImageError = true) }
                }
            }
        }
    }

    private fun dismissGameEditor() {
        gameEditorSession += 1
        val editingGame = controls.value.selectedGame
        val draftPath = editingGame?.customCoverImagePath
        val committedPath = editingGame?.let { savedGame(it.id)?.customCoverImagePath }
        controls.update {
            it.copy(
                selectedGame = null,
                isCoverImageLoading = false,
                hasCoverImageError = false,
            )
        }
        if (draftPath != committedPath) {
            viewModelScope.launch { coverImageStorage.remove(draftPath) }
        }
    }

    private fun deleteSelectedGames() {
        val selectedIds = controls.value.selectedGameIds
        val coverPaths = repository.games.value
            .filter { it.id in selectedIds }
            .mapNotNull { it.customCoverImagePath }
        repository.deleteGames(selectedIds)
        controls.update { it.copy(selectedGameIds = emptySet(), isDeleteConfirmationVisible = false) }
        viewModelScope.launch { coverPaths.forEach { coverImageStorage.remove(it) } }
    }

    private fun launchAddSearch(query: String): Job? {
        if (query.isBlank()) {
            _addSearchUiState.value = SearchUiState()
            return null
        }
        return viewModelScope.launch {
            delay(300.milliseconds)
            _addSearchUiState.value = _addSearchUiState.value.copy(status = SearchStatus.Loading)
            try {
                val results = withContext(Dispatchers.IO) { api.search(query) }
                _addSearchUiState.value = _addSearchUiState.value.copy(
                    results = results,
                    status = SearchStatus.Ready,
                )
            } catch (error: Throwable) {
                if (error is CancellationException) throw error
                _addSearchUiState.value = _addSearchUiState.value.copy(
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

    private fun savedGame(gameId: String): Game? = repository.games.value.find { it.id == gameId }
}

private data class BacklogControls(
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

private inline fun <T> MutableStateFlow<T>.update(transform: (T) -> T) {
    value = transform(value)
}
