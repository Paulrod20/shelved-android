package com.paulrod.shelved.data.sync

import com.paulrod.shelved.data.ActiveLibraryRepository
import com.paulrod.shelved.data.LibraryRepository
import com.paulrod.shelved.data.auth.AuthSessionProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/** Selects the correct local library for each auth session and imports trial data safely. */
class LibrarySessionCoordinator(
    private val persistentRepository: LibraryRepository,
    private val trialRepository: LibraryRepository,
    private val activeRepository: ActiveLibraryRepository,
    private val synchronizer: LibrarySynchronizer,
    private val sessionProvider: AuthSessionProvider,
    private val ownerStore: LibraryOwnerStore,
    private val scope: CoroutineScope,
) {
    fun start(): Job = scope.launch {
        sessionProvider.sessions
            .map { it.userId }
            .distinctUntilChanged()
            .collectLatest { userId ->
                if (userId == null) useFreshTrial() else usePersistentLibrary(userId)
            }
    }

    private fun useFreshTrial() {
        trialRepository.replaceLibrary(LibrarySnapshot())
        activeRepository.use(trialRepository)
    }

    private suspend fun usePersistentLibrary(userId: String) {
        val ownedLibrary = if (ownerStore.userId == null || ownerStore.userId == userId) {
            persistentRepository.snapshot()
        } else {
            LibrarySnapshot()
        }
        val importedLibrary = LibraryMerge.merge(trialRepository.snapshot(), ownedLibrary)
        persistentRepository.replaceLibrary(importedLibrary)
        ownerStore.userId = userId
        activeRepository.use(persistentRepository)
        trialRepository.replaceLibrary(LibrarySnapshot())
        synchronizer.synchronize(userId)
    }
}
