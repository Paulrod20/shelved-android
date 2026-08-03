package com.paulrod.shelved.ui.backlog

import com.paulrod.shelved.data.GameCatalog
import com.paulrod.shelved.data.ShelvedDataRepository
import com.paulrod.shelved.data.cover.CoverCropRequest
import com.paulrod.shelved.data.cover.GameCoverImageStorage
import com.paulrod.shelved.data.image.LocalImageSource
import com.paulrod.shelved.data.model.Game
import com.paulrod.shelved.test.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GameCoverLifecycleTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: FakeShelvedRepository
    private lateinit var storage: FakeGameCoverStorage
    private lateinit var viewModel: BacklogViewModel

    @Before
    fun setUp() {
        repository = FakeShelvedRepository(
            Game(id = "game", name = "Game", customCoverImagePath = OLD_COVER),
        )
        storage = FakeGameCoverStorage()
        viewModel = BacklogViewModel(repository, storage, UnusedGameCatalog)
    }

    @Test
    fun croppedCoverStaysDraftUntilGameIsSaved() = runTest {
        observeBacklogState()
        viewModel.onAction(BacklogAction.GameSelected(repository.games.value.single()))
        viewModel.onAction(BacklogAction.CoverCropConfirmed(cropRequest()))
        advanceUntilIdle()

        assertEquals(OLD_COVER, repository.games.value.single().customCoverImagePath)
        assertEquals(NEW_COVER, viewModel.uiState.value.selectedGame?.customCoverImagePath)

        viewModel.onAction(
            BacklogAction.GameSaved(requireNotNull(viewModel.uiState.value.selectedGame)),
        )
        advanceUntilIdle()

        assertEquals(NEW_COVER, repository.games.value.single().customCoverImagePath)
        assertEquals(listOf(OLD_COVER), storage.removedPaths)
        assertNull(viewModel.uiState.value.selectedGame)
    }

    @Test
    fun dismissingEditorDeletesDraftAndKeepsSavedCover() = runTest {
        observeBacklogState()
        viewModel.onAction(BacklogAction.GameSelected(repository.games.value.single()))
        viewModel.onAction(BacklogAction.CoverCropConfirmed(cropRequest()))
        advanceUntilIdle()

        viewModel.onAction(BacklogAction.GameDismissed)
        advanceUntilIdle()

        assertEquals(OLD_COVER, repository.games.value.single().customCoverImagePath)
        assertEquals(listOf(NEW_COVER), storage.removedPaths)
        assertNull(viewModel.uiState.value.selectedGame)
    }

    @Test
    fun useOriginalOnlyDeletesCommittedCoverAfterSave() = runTest {
        observeBacklogState()
        viewModel.onAction(BacklogAction.GameSelected(repository.games.value.single()))
        viewModel.onAction(BacklogAction.CustomCoverRemoved)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.selectedGame?.customCoverImagePath)
        assertEquals(OLD_COVER, repository.games.value.single().customCoverImagePath)
        assertFalse(storage.removedPaths.contains(OLD_COVER))

        viewModel.onAction(
            BacklogAction.GameSaved(requireNotNull(viewModel.uiState.value.selectedGame)),
        )
        advanceUntilIdle()

        assertNull(repository.games.value.single().customCoverImagePath)
        assertEquals(listOf(OLD_COVER), storage.removedPaths)
    }

    @Test
    fun deletingGameAlsoDeletesItsManagedCover() = runTest {
        observeBacklogState()
        viewModel.onAction(BacklogAction.GameLongPressed("game"))
        viewModel.onAction(BacklogAction.DeleteRequested)
        viewModel.onAction(BacklogAction.DeleteConfirmed)
        advanceUntilIdle()

        assertTrue(repository.games.value.isEmpty())
        assertEquals(listOf(OLD_COVER), storage.removedPaths)
    }

    private fun TestScope.observeBacklogState() {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
    }

    private fun cropRequest() = CoverCropRequest(
        source = LocalImageSource("content://cover"),
        zoom = 1f,
        horizontalOffset = 0f,
        verticalOffset = 0f,
    )

    private companion object {
        const val OLD_COVER = "/game_covers/old.jpg"
        const val NEW_COVER = "/game_covers/new.jpg"
    }
}

private object UnusedGameCatalog : GameCatalog {
    override suspend fun search(query: String): List<Game> = error("Not used")
    override suspend fun details(game: Game): Game = error("Not used")
}

private class FakeShelvedRepository(initialGame: Game) : ShelvedDataRepository {
    private val gameState = MutableStateFlow(listOf(initialGame))
    override val games: StateFlow<List<Game>> = gameState

    override fun addGame(game: Game) {
        gameState.value = listOf(game) + gameState.value
    }

    override fun updateGame(game: Game) {
        gameState.value = gameState.value.map { if (it.id == game.id) game else it }
    }

    override fun deleteGames(gameIds: Set<String>) {
        gameState.value = gameState.value.filterNot { it.id in gameIds }
    }
}

private class FakeGameCoverStorage : GameCoverImageStorage {
    val removedPaths = mutableListOf<String>()

    override suspend fun save(request: CoverCropRequest): String = "/game_covers/new.jpg"

    override suspend fun remove(path: String?) {
        path?.let(removedPaths::add)
    }

    override suspend fun prune(referencedPaths: Set<String>) = Unit
}
