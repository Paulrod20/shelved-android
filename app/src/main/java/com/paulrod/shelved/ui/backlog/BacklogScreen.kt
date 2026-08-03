package com.paulrod.shelved.ui.backlog

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paulrod.shelved.data.model.Game
import com.paulrod.shelved.data.model.GameStatus
import com.paulrod.shelved.ui.components.EmptyLibrary
import com.paulrod.shelved.ui.components.FilterPill
import com.paulrod.shelved.ui.components.SearchField
import com.paulrod.shelved.ui.components.ShelvedScreen
import com.paulrod.shelved.ui.components.label
import com.paulrod.shelved.ui.search.SearchUiState
import com.paulrod.shelved.ui.theme.TextMuted
import com.paulrod.shelved.ui.theme.TextPrimary

@Composable
fun BacklogScreen(
    state: BacklogUiState,
    addSearchState: SearchUiState,
    onAction: (BacklogAction) -> Unit,
    onAddSearchQueryChanged: (String) -> Unit,
    onAddSearchRetry: () -> Unit,
    onAddSearchGameSelected: (Game?) -> Unit,
) {
    val isSelecting = state.selectedGameIds.isNotEmpty()
    BackHandler(enabled = isSelecting) { onAction(BacklogAction.SelectionCleared) }

    ShelvedScreen(
        title = if (isSelecting) "${state.selectedGameIds.size} SELECTED" else "SHELVED",
        actions = { BacklogHeader(state, onAction) },
    ) {
        if (state.isSearchVisible) {
            SearchField(state.searchQuery, "Search your shelf…") {
                onAction(BacklogAction.SearchChanged(it))
            }
        }
        StatusFilters(state.statusFilter) { onAction(BacklogAction.StatusSelected(it)) }
        when {
            state.visibleGames.isNotEmpty() -> BacklogGameGrid(
                games = state.visibleGames,
                selectedGameIds = state.selectedGameIds,
                onSelect = { game ->
                    onAction(
                        if (isSelecting) BacklogAction.GameSelectionToggled(game.id)
                        else BacklogAction.GameSelected(game),
                    )
                },
                onLongPress = { onAction(BacklogAction.GameLongPressed(it.id)) },
            )
            state.searchQuery.isNotBlank() -> NoBacklogMatches(state.searchQuery, Modifier.fillMaxSize())
            else -> EmptyLibrary(state.statusFilter == null, Modifier.fillMaxSize()) {
                onAction(BacklogAction.AddRequested)
            }
        }
    }

    if (state.isAddSheetVisible) {
        AddGameSheet(
            state = addSearchState,
            onClose = { onAction(BacklogAction.AddDismissed) },
            onQueryChanged = onAddSearchQueryChanged,
            onRetry = onAddSearchRetry,
            onGameSelected = onAddSearchGameSelected,
            onAdd = { onAction(BacklogAction.GameAdded(it)) },
        )
    }
    if (state.isSortSheetVisible) {
        BacklogSortSheet(
            selected = state.sortOrder,
            onClose = { onAction(BacklogAction.SortDismissed) },
            onSelect = { onAction(BacklogAction.SortSelected(it)) },
        )
    }
    state.selectedGame?.let { game ->
        EditGameSheet(
            game = game,
            isCoverLoading = state.isCoverImageLoading,
            hasCoverError = state.hasCoverImageError,
            onClose = { onAction(BacklogAction.GameDismissed) },
            onSave = { onAction(BacklogAction.GameSaved(it)) },
            onCoverCrop = { onAction(BacklogAction.CoverCropConfirmed(it)) },
            onCoverRemoved = { onAction(BacklogAction.CustomCoverRemoved) },
        )
    }
    if (state.isDeleteConfirmationVisible) {
        DeleteGamesDialog(
            gameCount = state.selectedGameIds.size,
            onDismiss = { onAction(BacklogAction.DeleteDismissed) },
            onConfirm = { onAction(BacklogAction.DeleteConfirmed) },
        )
    }
}

@Composable
private fun StatusFilters(selected: GameStatus?, onSelect: (GameStatus?) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 16.dp)) {
        item { FilterPill("All", selected == null) { onSelect(null) } }
        items(GameStatus.entries) { status -> FilterPill(status.label, selected == status) { onSelect(status) } }
    }
}

@Composable
private fun NoBacklogMatches(query: String, modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Outlined.Search, null, tint = TextMuted, modifier = Modifier.size(44.dp))
        Text(
            "No games found",
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 14.dp),
        )
        Text("Nothing on your shelf matches “$query”.", color = TextMuted, fontSize = 13.sp)
    }
}
