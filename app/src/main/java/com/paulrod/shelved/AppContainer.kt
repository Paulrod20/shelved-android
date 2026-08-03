package com.paulrod.shelved

import android.content.Context
import com.paulrod.shelved.data.CloudflareGameCatalog
import com.paulrod.shelved.data.GameCatalog
import com.paulrod.shelved.data.ShelvedRepository
import com.paulrod.shelved.data.auth.AuthRepository
import com.paulrod.shelved.data.auth.GoogleSignInClient
import com.paulrod.shelved.data.cover.GameCoverImageStore
import com.paulrod.shelved.data.profile.ProfileImageStore
import com.paulrod.shelved.data.sync.FirestoreCloudLibraryStore
import com.paulrod.shelved.data.sync.SharedPreferencesLibraryOwnerStore

/** Application-scoped dependencies shared by the UI without a DI framework. */
internal class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val repository by lazy { ShelvedRepository.getInstance(appContext) }
    val gameCatalog: GameCatalog by lazy { CloudflareGameCatalog() }
    val authRepository by lazy { AuthRepository() }
    val googleSignInClient by lazy { GoogleSignInClient(appContext) }
    val profileImageStore by lazy { ProfileImageStore(appContext) }
    val gameCoverImageStore by lazy { GameCoverImageStore(appContext) }
    val cloudLibraryStore by lazy { FirestoreCloudLibraryStore() }
    val libraryOwnerStore by lazy { SharedPreferencesLibraryOwnerStore(appContext) }
}
