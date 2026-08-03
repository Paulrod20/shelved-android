package com.paulrod.shelved.ui.navigation

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.paulrod.shelved.AppContainer
import com.paulrod.shelved.data.sync.LibrarySyncCoordinator
import com.paulrod.shelved.ui.ShelvedViewModel
import com.paulrod.shelved.ui.account.AccountViewModel
import com.paulrod.shelved.ui.account.AccountUiState
import com.paulrod.shelved.ui.account.SignInScreen
import com.paulrod.shelved.ui.backlog.BacklogScreen
import com.paulrod.shelved.ui.backlog.BacklogViewModel
import com.paulrod.shelved.ui.profile.ProfileScreen
import com.paulrod.shelved.ui.profile.ProfileViewModel
import com.paulrod.shelved.ui.search.SearchScreen
import com.paulrod.shelved.ui.stats.StatsScreen
import com.paulrod.shelved.ui.theme.Background

@Composable
internal fun MainApp(
    container: AppContainer,
    activityContext: Context,
) {
    val repository = container.repository
    val imageStore = container.profileImageStore
    val coverImageStore = container.gameCoverImageStore
    val authRepository = container.authRepository
    val syncScope = rememberCoroutineScope()
    val syncCoordinator = remember(container, syncScope) {
        LibrarySyncCoordinator(
            localStore = repository,
            cloudStore = container.cloudLibraryStore,
            sessionProvider = authRepository,
            ownerStore = container.libraryOwnerStore,
            scope = syncScope,
            onError = { error -> Log.e("LibrarySync", "Cloud library sync failed.", error) },
        )
    }
    DisposableEffect(syncCoordinator) {
        val syncJob = syncCoordinator.start()
        onDispose(syncJob::cancel)
    }
    LaunchedEffect(repository, imageStore, coverImageStore) {
        imageStore.prune(setOfNotNull(repository.profile.value.profileImagePath))
        coverImageStore.prune(repository.games.value.mapNotNullTo(mutableSetOf()) { it.customCoverImagePath })
    }
    val shelvedViewModel: ShelvedViewModel = viewModel {
        ShelvedViewModel(repository, container.gameCatalog)
    }
    val backlogViewModel: BacklogViewModel = viewModel {
        BacklogViewModel(repository, coverImageStore, container.gameCatalog)
    }
    val profileViewModel: ProfileViewModel = viewModel {
        ProfileViewModel(repository, imageStore)
    }
    val accountViewModel: AccountViewModel = viewModel {
        AccountViewModel(authRepository, authRepository)
    }
    val accountState by accountViewModel.uiState.collectAsStateWithLifecycle()

    if (accountState.isSignInVisible) {
        SignInScreen(
            state = accountState,
            onBack = accountViewModel::dismissSignIn,
            onGoogleSignIn = {
                accountViewModel.signInWithGoogle {
                    container.googleSignInClient.getIdToken(activityContext)
                }
            },
            onEmailSignIn = accountViewModel::signInWithEmail,
            onPasswordReset = accountViewModel::sendPasswordReset,
            onClearFeedback = accountViewModel::clearFeedback,
        )
        return
    }

    MainNavigation(shelvedViewModel, backlogViewModel, profileViewModel, accountViewModel, accountState)
}

@Composable
private fun MainNavigation(
    shelvedViewModel: ShelvedViewModel,
    backlogViewModel: BacklogViewModel,
    profileViewModel: ProfileViewModel,
    accountViewModel: AccountViewModel,
    accountState: AccountUiState,
) {
    var destination by rememberSaveable { mutableStateOf(Destination.BACKLOG) }
    val backlogState by backlogViewModel.uiState.collectAsStateWithLifecycle()
    val addSearchState by backlogViewModel.addSearchUiState.collectAsStateWithLifecycle()
    val searchState by shelvedViewModel.searchUiState.collectAsStateWithLifecycle()
    val profileState by profileViewModel.uiState.collectAsStateWithLifecycle()
    val statsState by shelvedViewModel.statsUiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Background,
        bottomBar = { FloatingNavigation(destination) { destination = it } },
    ) { contentPadding ->
        Box(Modifier.fillMaxSize().padding(bottom = contentPadding.calculateBottomPadding())) {
            when (destination) {
                Destination.BACKLOG -> BacklogScreen(
                    state = backlogState,
                    addSearchState = addSearchState,
                    onAction = backlogViewModel::onAction,
                    onAddSearchQueryChanged = backlogViewModel::onAddSearchQueryChanged,
                    onAddSearchRetry = backlogViewModel::retryAddSearch,
                    onAddSearchGameSelected = backlogViewModel::onAddSearchGameSelected,
                )
                Destination.SEARCH -> SearchScreen(
                    state = searchState,
                    onAction = shelvedViewModel::onSearchAction,
                )
                Destination.STATS -> StatsScreen(statsState)
                Destination.PROFILE -> ProfileScreen(
                    state = profileState,
                    accountSession = accountState.session,
                    onAction = profileViewModel::onAction,
                    onSignIn = accountViewModel::showSignIn,
                    onSignOut = accountViewModel::signOut,
                )
            }
        }
    }
}
