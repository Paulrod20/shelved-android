package com.paulrod.shelved.data

import com.paulrod.shelved.data.model.Game
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ActiveLibraryRepositoryTest {
    @Test
    fun switchingLibrariesUpdatesReadsAndRoutesFutureWrites() = runTest {
        val trial = InMemoryLibraryRepository().apply { addGame(Game("trial", "Trial game")) }
        val persistent = InMemoryLibraryRepository().apply { addGame(Game("saved", "Saved game")) }
        val active = ActiveLibraryRepository(trial, backgroundScope)
        runCurrent()

        assertEquals(listOf("trial"), active.games.value.map(Game::id))

        active.use(persistent)
        runCurrent()
        active.addGame(Game("new", "New game"))
        runCurrent()

        assertEquals(listOf("new", "saved"), active.games.value.map(Game::id))
        assertEquals(listOf("new", "saved"), persistent.games.value.map(Game::id))
        assertTrue(trial.games.value.none { it.id == "new" })
    }
}
