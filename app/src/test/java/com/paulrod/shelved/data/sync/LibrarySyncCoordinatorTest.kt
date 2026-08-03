package com.paulrod.shelved.data.sync

import com.paulrod.shelved.data.auth.AuthSession
import com.paulrod.shelved.data.auth.AuthSessionProvider
import com.paulrod.shelved.data.model.Game
import com.paulrod.shelved.data.model.Profile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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
    fun signedInUserRestoresCloudLibraryIntoAnEmptyDevice() = runTest {
        val local = FakeLocalStore()
        val cloudLibrary = LibrarySnapshot(games = listOf(Game("cloud", "Cloud game")))
        val cloud = FakeCloudStore(cloudLibrary)
        val sessions = FakeSessionProvider(AuthSession(userId = "user"))
        LibrarySyncCoordinator(
            local, cloud, sessions, FakeOwnerStore(), backgroundScope, Duration.ZERO,
        ).start()

        runCurrent()

        assertEquals(listOf("cloud"), local.games.value.map(Game::id))
        assertTrue(cloud.appliedChanges.isEmpty())
    }

    @Test
    fun localChangesAreBackedUpAfterInitialMerge() = runTest {
        val local = FakeLocalStore(games = listOf(Game("local", "Local game")))
        val cloud = FakeCloudStore(null)
        val sessions = FakeSessionProvider(AuthSession(userId = "user"))
        LibrarySyncCoordinator(
            local, cloud, sessions, FakeOwnerStore(), backgroundScope, Duration.ZERO,
        ).start()
        runCurrent()

        assertEquals(listOf("local"), cloud.appliedChanges.single().gamesToUpsert.map(Game::id))

        local.setGames(local.games.value + Game("second", "Second game"))
        advanceTimeBy(1)
        advanceUntilIdle()

        assertEquals(listOf("second"), cloud.appliedChanges.last().gamesToUpsert.map(Game::id))
    }

    @Test
    fun signedOutUsersNeverReadOrWriteCloudData() = runTest {
        val cloud = FakeCloudStore(null)
        val sessions = FakeSessionProvider(AuthSession())
        LibrarySyncCoordinator(
            FakeLocalStore(), cloud, sessions, FakeOwnerStore(), backgroundScope, Duration.ZERO,
        ).start()

        runCurrent()

        assertTrue(cloud.loadedUserIds.isEmpty())
        assertTrue(cloud.appliedChanges.isEmpty())
    }

    @Test
    fun switchingAccountsDoesNotUploadThePreviousAccountsLocalLibrary() = runTest {
        val local = FakeLocalStore(games = listOf(Game("private", "Previous account game")))
        val newAccountLibrary = LibrarySnapshot(games = listOf(Game("new", "New account game")))
        val cloud = FakeCloudStore(newAccountLibrary)
        val sessions = FakeSessionProvider(AuthSession(userId = "new-user"))
        val owner = FakeOwnerStore(userId = "previous-user")
        LibrarySyncCoordinator(
            local, cloud, sessions, owner, backgroundScope, Duration.ZERO,
        ).start()

        runCurrent()

        assertEquals(listOf("new"), local.games.value.map(Game::id))
        assertTrue(cloud.appliedChanges.isEmpty())
        assertEquals("new-user", owner.userId)
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
        val loadedUserIds = mutableListOf<String>()
        val appliedChanges = mutableListOf<LibraryChanges>()

        override suspend fun load(userId: String): LibrarySnapshot? {
            loadedUserIds += userId
            return library
        }

        override suspend fun apply(userId: String, changes: LibraryChanges) {
            appliedChanges += changes
        }
    }

    private class FakeSessionProvider(initial: AuthSession) : AuthSessionProvider {
        private val state = MutableStateFlow(initial)
        override val currentSession: AuthSession get() = state.value
        override val sessions: Flow<AuthSession> = state
    }

    private class FakeOwnerStore(override var userId: String? = null) : LibraryOwnerStore
}
