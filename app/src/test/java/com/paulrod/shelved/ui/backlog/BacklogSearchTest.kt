package com.paulrod.shelved.ui.backlog

import com.paulrod.shelved.data.GameCatalog
import com.paulrod.shelved.data.ShelvedDataRepository
import com.paulrod.shelved.data.cover.CoverCropRequest
import com.paulrod.shelved.data.cover.GameCoverImageStorage
import com.paulrod.shelved.data.model.Game
import com.paulrod.shelved.test.MainDispatcherRule
import com.paulrod.shelved.ui.search.SearchFailure
import com.paulrod.shelved.ui.search.SearchStatus
import java.net.SocketTimeoutException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BacklogSearchTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun retryImmediatelyRepeatsTheCurrentQuery() = runTest {
        val catalog = FailsOnceCatalog()
        val viewModel = BacklogViewModel(
            EmptyGameRepository(),
            NoopCoverStorage(),
            catalog,
            mainDispatcherRule.dispatcher,
        )

        viewModel.onAddSearchQueryChanged("Mario")
        advanceUntilIdle()

        assertEquals(
            SearchStatus.Error(SearchFailure.SERVICE_UNAVAILABLE),
            viewModel.addSearchUiState.value.status,
        )

        viewModel.retryAddSearch()
        advanceUntilIdle()

        assertEquals(2, catalog.searchCount)
        assertEquals(SearchStatus.Ready, viewModel.addSearchUiState.value.status)
        assertEquals(listOf("Mario"), viewModel.addSearchUiState.value.results.map(Game::name))
    }

    private class FailsOnceCatalog : GameCatalog {
        var searchCount = 0

        override suspend fun search(query: String): List<Game> {
            searchCount += 1
            if (searchCount == 1) throw SocketTimeoutException()
            return listOf(Game(id = "mario", name = query))
        }

        override suspend fun details(game: Game) = game
    }

    private class EmptyGameRepository : ShelvedDataRepository {
        override val games: StateFlow<List<Game>> = MutableStateFlow(emptyList())
        override fun addGame(game: Game) = Unit
        override fun updateGame(game: Game) = Unit
        override fun deleteGames(gameIds: Set<String>) = Unit
    }

    private class NoopCoverStorage : GameCoverImageStorage {
        override suspend fun save(request: CoverCropRequest) = error("Not used")
        override suspend fun remove(path: String?) = Unit
        override suspend fun prune(referencedPaths: Set<String>) = Unit
    }
}
