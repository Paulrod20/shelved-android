package com.paulrod.shelved.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.paulrod.shelved.ui.ShelvedViewModel
import com.paulrod.shelved.ui.backlog.BacklogScreen
import com.paulrod.shelved.ui.profile.ProfileScreen
import com.paulrod.shelved.ui.search.SearchScreen
import com.paulrod.shelved.ui.stats.StatsScreen
import com.paulrod.shelved.ui.theme.Background

@Composable
fun ShelvedApp(viewModel: ShelvedViewModel = viewModel()) {
    var destination by rememberSaveable { mutableStateOf(Destination.BACKLOG) }
    val backlogState by viewModel.backlogUiState.collectAsState()
    val addSearchState by viewModel.addSearchUiState.collectAsState()
    val searchState by viewModel.searchUiState.collectAsState()
    val profileState by viewModel.profileUiState.collectAsState()
    val statsState by viewModel.statsUiState.collectAsState()

    Scaffold(
        containerColor = Background,
        bottomBar = { FloatingNavigation(destination) { destination = it } },
    ) { contentPadding ->
        Box(Modifier.fillMaxSize().padding(bottom = contentPadding.calculateBottomPadding())) {
            when (destination) {
                Destination.BACKLOG -> BacklogScreen(
                    state = backlogState,
                    addSearchState = addSearchState,
                    onAction = viewModel::onBacklogAction,
                    onAddSearchQueryChanged = viewModel::onAddSearchQueryChanged,
                    onAddSearchGameSelected = viewModel::onAddSearchGameSelected,
                )
                Destination.SEARCH -> SearchScreen(
                    state = searchState,
                    onAction = viewModel::onSearchAction,
                )
                Destination.STATS -> StatsScreen(statsState)
                Destination.PROFILE -> ProfileScreen(profileState, viewModel::onProfileAction)
            }
        }
    }
}
