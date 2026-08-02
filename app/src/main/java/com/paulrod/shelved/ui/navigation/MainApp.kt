package com.paulrod.shelved.ui.navigation

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.paulrod.shelved.data.ShelvedRepository
import com.paulrod.shelved.data.cover.GameCoverImageStore
import com.paulrod.shelved.data.auth.AuthGateway
import com.paulrod.shelved.data.auth.AuthSessionProvider
import com.paulrod.shelved.data.auth.GoogleSignInClient
import com.paulrod.shelved.data.profile.ProfileImageStore
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
    authGateway: AuthGateway,
    sessionProvider: AuthSessionProvider,
    googleSignInClient: GoogleSignInClient,
    activityContext: Context,
) {
    val appContext = activityContext.applicationContext
    val repository = remember(appContext) { ShelvedRepository(appContext) }
    val imageStore = remember(appContext) { ProfileImageStore(appContext) }
    val coverImageStore = remember(appContext) { GameCoverImageStore(appContext) }
    LaunchedEffect(repository, imageStore, coverImageStore) {
        imageStore.prune(setOfNotNull(repository.profile.value.profileImagePath))
        coverImageStore.prune(repository.games.value.mapNotNullTo(mutableSetOf()) { it.customCoverImagePath })
    }
    val shelvedViewModel: ShelvedViewModel = viewModel { ShelvedViewModel(repository) }
    val backlogViewModel: BacklogViewModel = viewModel {
        BacklogViewModel(repository, coverImageStore)
    }
    val profileViewModel: ProfileViewModel = viewModel {
        ProfileViewModel(repository, imageStore)
    }
    val accountViewModel: AccountViewModel = viewModel {
        AccountViewModel(authGateway, sessionProvider)
    }
    val accountState by accountViewModel.uiState.collectAsState()

    if (accountState.isSignInVisible) {
        SignInScreen(
            state = accountState,
            onBack = accountViewModel::dismissSignIn,
            onGoogleSignIn = {
                accountViewModel.signInWithGoogle {
                    googleSignInClient.getIdToken(activityContext)
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
    val backlogState by backlogViewModel.uiState.collectAsState()
    val addSearchState by backlogViewModel.addSearchUiState.collectAsState()
    val searchState by shelvedViewModel.searchUiState.collectAsState()
    val profileState by profileViewModel.uiState.collectAsState()
    val statsState by shelvedViewModel.statsUiState.collectAsState()

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
