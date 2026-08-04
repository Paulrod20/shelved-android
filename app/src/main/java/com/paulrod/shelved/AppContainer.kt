package com.paulrod.shelved

import android.content.Context
import com.paulrod.shelved.data.ActiveLibraryRepository
import com.paulrod.shelved.data.CloudflareGameCatalog
import com.paulrod.shelved.data.GameCatalog
import com.paulrod.shelved.data.InMemoryLibraryRepository
import com.paulrod.shelved.data.ShelvedRepository
import com.paulrod.shelved.data.auth.AuthRepository
import com.paulrod.shelved.data.auth.GoogleSignInClient
import com.paulrod.shelved.data.cover.GameCoverImageStore
import com.paulrod.shelved.data.profile.ProfileImageStore
import com.paulrod.shelved.data.sync.FirestoreCloudLibraryStore
import com.paulrod.shelved.data.sync.LibrarySnapshot
import com.paulrod.shelved.data.sync.SharedPreferencesLibraryOwnerStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/** Application-scoped dependencies shared by the UI without a DI framework. */
internal class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    val persistentRepository by lazy { ShelvedRepository.getInstance(appContext) }
    val trialRepository by lazy { InMemoryLibraryRepository() }
    val activeRepository by lazy {
        val initial = if (authRepository.currentSession.isSignedIn) persistentRepository else trialRepository
        ActiveLibraryRepository(initial, applicationScope)
    }
    val gameCatalog: GameCatalog by lazy { CloudflareGameCatalog() }
    val authRepository by lazy { AuthRepository() }
    val googleSignInClient by lazy { GoogleSignInClient(appContext) }
    val profileImageStore by lazy { ProfileImageStore(appContext) }
    val gameCoverImageStore by lazy { GameCoverImageStore(appContext) }
    val cloudLibraryStore by lazy { FirestoreCloudLibraryStore() }
    val libraryOwnerStore by lazy { SharedPreferencesLibraryOwnerStore(appContext) }

    fun discardTrialIfSignedOut() {
        if (!authRepository.currentSession.isSignedIn) {
            trialRepository.replaceLibrary(LibrarySnapshot())
        }
    }
}
