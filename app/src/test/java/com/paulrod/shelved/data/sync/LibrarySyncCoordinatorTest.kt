package com.paulrod.shelved.data.sync

import com.paulrod.shelved.data.model.Game
import com.paulrod.shelved.data.model.Profile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Duration

@OptIn(ExperimentalCoroutinesApi::class)
class LibrarySyncCoordinatorTest {
    @Test
    fun restoresCloudLibraryIntoAnEmptyDevice() = runTest {
        val local = FakeLocalStore()
        val cloud = FakeCloudStore(LibrarySnapshot(games = listOf(Game("cloud", "Cloud game"))))
        val coordinator = LibrarySyncCoordinator(local, cloud, Duration.ZERO)

        backgroundScope.launch { coordinator.synchronize("user") }
        runCurrent()

        assertEquals(listOf("cloud"), local.games.value.map(Game::id))
        assertTrue(cloud.appliedChanges.isEmpty())
    }

    @Test
    fun localChangesAreBackedUpAfterInitialMerge() = runTest {
        val local = FakeLocalStore(games = listOf(Game("local", "Local game")))
        val cloud = FakeCloudStore(null)
        val coordinator = LibrarySyncCoordinator(local, cloud, Duration.ZERO)

        backgroundScope.launch { coordinator.synchronize("user") }
        runCurrent()

        assertEquals(listOf("local"), cloud.appliedChanges.single().gamesToUpsert.map(Game::id))

        local.setGames(local.games.value + Game("second", "Second game"))
        advanceTimeBy(1)
        advanceUntilIdle()

        assertEquals(listOf("second"), cloud.appliedChanges.last().gamesToUpsert.map(Game::id))
    }

    private class FakeLocalStore(
        games: List<Game> = emptyList(),
        profile: Profile = Profile(),
    ) : LocalLibraryStore {
        override val games = MutableStateFlow(games)
        override val profile = MutableStateFlow(profile)

        override fun replaceLibrary(snapshot: LibrarySnapshot) {
            games.value = snapshot.games
            profile.value = snapshot.profile
        }

        fun setGames(value: List<Game>) {
            games.value = value
        }
    }

    private class FakeCloudStore(private val library: LibrarySnapshot?) : CloudLibraryStore {
        val appliedChanges = mutableListOf<LibraryChanges>()

        override suspend fun load(userId: String): LibrarySnapshot? = library

        override suspend fun apply(userId: String, changes: LibraryChanges) {
            appliedChanges += changes
        }
    }
}
