package com.paulrod.shelved.data.sync

import com.paulrod.shelved.data.ActiveLibraryRepository
import com.paulrod.shelved.data.InMemoryLibraryRepository
import com.paulrod.shelved.data.auth.AuthSession
import com.paulrod.shelved.data.auth.AuthSessionProvider
import com.paulrod.shelved.data.model.Game
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LibrarySessionCoordinatorTest {
    @Test
    fun signedOutSessionStartsWithAFreshTrialAndNeverSynchronizes() = runTest {
        val persistent = libraryWith("saved")
        val trial = libraryWith("old-trial")
        val synchronizer = FakeSynchronizer(persistent)
        val active = ActiveLibraryRepository(persistent, backgroundScope)
        coordinator(
            persistent = persistent,
            trial = trial,
            active = active,
            synchronizer = synchronizer,
            session = AuthSession(),
            scope = backgroundScope,
        ).start()

        runCurrent()

        assertTrue(trial.games.value.isEmpty())
        assertTrue(active.games.value.isEmpty())
        assertTrue(synchronizer.userIds.isEmpty())
    }

    @Test
    fun signingInImportsAndClearsTheTrialLibrary() = runTest {
        val persistent = InMemoryLibraryRepository()
        val trial = libraryWith("trial")
        val synchronizer = FakeSynchronizer(persistent)
        val active = ActiveLibraryRepository(trial, backgroundScope)
        coordinator(
            persistent,
            trial,
            active,
            synchronizer,
            AuthSession(userId = "user"),
            backgroundScope,
        ).start()

        runCurrent()

        assertEquals(listOf("trial"), persistent.games.value.map(Game::id))
        assertEquals(listOf("trial"), active.games.value.map(Game::id))
        assertTrue(trial.games.value.isEmpty())
        assertEquals(listOf("user"), synchronizer.userIds)
    }

    @Test
    fun accountSwitchImportsTrialButExcludesThePreviousAccountsLibrary() = runTest {
        val persistent = libraryWith("private")
        val trial = libraryWith("trial")
        val synchronizer = FakeSynchronizer(persistent)
        val active = ActiveLibraryRepository(trial, backgroundScope)
        val owner = FakeOwnerStore("previous-user")
        LibrarySessionCoordinator(
            persistent,
            trial,
            active,
            synchronizer,
            FakeSessionProvider(AuthSession(userId = "new-user")),
            owner,
            backgroundScope,
        ).start()

        runCurrent()

        assertEquals(listOf("trial"), synchronizer.snapshots.single().games.map(Game::id))
        assertTrue(persistent.games.value.none { it.id == "private" })
        assertEquals("new-user", owner.userId)
    }

    private fun coordinator(
        persistent: InMemoryLibraryRepository,
        trial: InMemoryLibraryRepository,
        active: ActiveLibraryRepository,
        synchronizer: FakeSynchronizer,
        session: AuthSession,
        scope: CoroutineScope,
    ) = LibrarySessionCoordinator(
        persistent,
        trial,
        active,
        synchronizer,
        FakeSessionProvider(session),
        FakeOwnerStore(),
        scope,
    )

    private fun libraryWith(gameId: String) = InMemoryLibraryRepository().apply {
        addGame(Game(gameId, "$gameId game"))
    }

    private class FakeSynchronizer(
        private val persistent: InMemoryLibraryRepository,
    ) : LibrarySynchronizer {
        val userIds = mutableListOf<String>()
        val snapshots = mutableListOf<LibrarySnapshot>()

        override suspend fun synchronize(userId: String) {
            userIds += userId
            snapshots += persistent.snapshot()
        }
    }

    private class FakeSessionProvider(initial: AuthSession) : AuthSessionProvider {
        private val state = MutableStateFlow(initial)
        override val currentSession: AuthSession get() = state.value
        override val sessions: Flow<AuthSession> = state
    }

    private class FakeOwnerStore(override var userId: String? = null) : LibraryOwnerStore
}
