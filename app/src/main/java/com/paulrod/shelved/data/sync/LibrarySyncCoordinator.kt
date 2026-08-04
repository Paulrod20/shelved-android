package com.paulrod.shelved.data.sync

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

interface LibrarySynchronizer {
    suspend fun synchronize(userId: String)
}

/** Loads and continuously backs up one authenticated user's persistent library. */
class LibrarySyncCoordinator(
    private val localStore: LocalLibraryStore,
    private val cloudStore: CloudLibraryStore,
    private val backupDelay: Duration = 500.milliseconds,
    private val onError: (Throwable) -> Unit = {},
) : LibrarySynchronizer {
    @OptIn(FlowPreview::class)
    override suspend fun synchronize(userId: String) {
        val cloud = try {
            cloudStore.load(userId)
        } catch (error: Throwable) {
            if (error is CancellationException) throw error
            onError(error)
            return
        }

        val merged = LibraryMerge.merge(localStore.snapshot(), cloud)
        localStore.replaceLibrary(merged)
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
}

internal fun LocalLibraryStore.snapshot() = LibrarySnapshot(games.value, profile.value)
