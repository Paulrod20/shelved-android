package com.paulrod.shelved.data.sync

import com.paulrod.shelved.data.auth.AuthSessionProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class LibrarySyncCoordinator(
    private val localStore: LocalLibraryStore,
    private val cloudStore: CloudLibraryStore,
    private val sessionProvider: AuthSessionProvider,
    private val ownerStore: LibraryOwnerStore,
    private val scope: CoroutineScope,
    private val backupDelay: Duration = 500.milliseconds,
    private val onError: (Throwable) -> Unit = {},
) {
    fun start(): Job = scope.launch {
        sessionProvider.sessions
            .map { it.userId }
            .distinctUntilChanged()
            .collectLatest { userId ->
                if (userId != null) synchronize(userId)
            }
    }

    @OptIn(FlowPreview::class)
    private suspend fun synchronize(userId: String) {
        val cloud = try {
            cloudStore.load(userId)
        } catch (error: Throwable) {
            if (error is CancellationException) throw error
            onError(error)
            return
        }

        val localOwnerId = ownerStore.userId
        val merged = if (localOwnerId == null || localOwnerId == userId) {
            LibraryMerge.merge(localStore.snapshot(), cloud)
        } else {
            // Never copy one account's on-device library into a different account.
            cloud ?: LibrarySnapshot()
        }
        localStore.replaceLibrary(merged)
        ownerStore.userId = userId
        var lastCloudSnapshot = cloud?.withoutDeviceOnlyData()
        lastCloudSnapshot = backUp(userId, merged, lastCloudSnapshot)

        combine(localStore.games, localStore.profile, ::LibrarySnapshot)
            .debounce(backupDelay)
            .collect { current ->
                lastCloudSnapshot = backUp(userId, current, lastCloudSnapshot)
            }
    }

    private suspend fun backUp(
        userId: String,
        current: LibrarySnapshot,
        previous: LibrarySnapshot?,
    ): LibrarySnapshot? {
        val cloudSafeSnapshot = current.withoutDeviceOnlyData()
        val changes = cloudSafeSnapshot.changesSince(previous)
        if (changes.isEmpty) return cloudSafeSnapshot

        return try {
            cloudStore.apply(userId, changes)
            cloudSafeSnapshot
        } catch (error: Throwable) {
            if (error is CancellationException) throw error
            onError(error)
            previous
        }
    }

    private fun LocalLibraryStore.snapshot() = LibrarySnapshot(games.value, profile.value)
}
