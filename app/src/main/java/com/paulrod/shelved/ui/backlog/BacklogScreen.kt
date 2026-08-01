package com.paulrod.shelved.ui.backlog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.paulrod.shelved.data.model.Game
import com.paulrod.shelved.data.model.GameStatus
import com.paulrod.shelved.ui.components.EmptyLibrary
import com.paulrod.shelved.ui.components.CatalogGameSheet
import com.paulrod.shelved.ui.components.FilterPill
import com.paulrod.shelved.ui.components.GameCard
import com.paulrod.shelved.ui.components.GameCover
import com.paulrod.shelved.ui.components.PrimaryButton
import com.paulrod.shelved.ui.components.SearchField
import com.paulrod.shelved.ui.components.SearchResultRow
import com.paulrod.shelved.ui.components.SectionLabel
import com.paulrod.shelved.ui.components.ShelvedField
import com.paulrod.shelved.ui.components.ShelvedScreen
import com.paulrod.shelved.ui.components.ShelvedSheet
import com.paulrod.shelved.ui.components.StatusPicker
import com.paulrod.shelved.ui.components.label
import com.paulrod.shelved.ui.search.SearchStatus
import com.paulrod.shelved.ui.search.SearchUiState
import com.paulrod.shelved.ui.theme.Accent
import com.paulrod.shelved.ui.theme.AccentText
import com.paulrod.shelved.ui.theme.SurfaceElevated
import com.paulrod.shelved.ui.theme.TextMuted
import com.paulrod.shelved.ui.theme.TextPrimary

@Composable
fun BacklogScreen(
    state: BacklogUiState,
    addSearchState: SearchUiState,
    onAction: (BacklogAction) -> Unit,
    onAddSearchQueryChanged: (String) -> Unit,
    onAddSearchGameSelected: (Game?) -> Unit,
) {
    ShelvedScreen("SHELVED", actions = {
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            HeaderAction(Icons.Default.Search, "Search your backlog", state.isSearchVisible) {
                onAction(BacklogAction.ToggleSearch)
            }
            HeaderAction(
                Icons.AutoMirrored.Filled.Sort,
                "Sort backlog",
                state.sortOrder != BacklogSort.RECENTLY_ADDED,
            ) { onAction(BacklogAction.SortRequested) }
            FloatingActionButton(
                onClick = { onAction(BacklogAction.AddRequested) },
                modifier = Modifier.size(40.dp),
                containerColor = Accent,
                contentColor = AccentText,
                shape = CircleShape,
            ) { Icon(Icons.Default.Add, "Add game") }
        }
    }) {
        if (state.isSearchVisible) {
            SearchField(state.searchQuery, "Search your shelf…") {
                onAction(BacklogAction.SearchChanged(it))
            }
        }
        StatusFilters(state.statusFilter) { onAction(BacklogAction.StatusSelected(it)) }
        when {
            state.visibleGames.isNotEmpty() -> GameGrid(state.visibleGames) {
                onAction(BacklogAction.GameSelected(it))
            }
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
            onClose = { onAction(BacklogAction.GameDismissed) },
            onSave = { onAction(BacklogAction.GameSaved(it)) },
        )
    }
}

@Composable
private fun HeaderAction(icon: ImageVector, description: String, active: Boolean, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(40.dp),
    ) {
        Icon(icon, description, tint = if (active) Accent else TextPrimary, modifier = Modifier.size(21.dp))
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
private fun GameGrid(games: List<Game>, onSelect: (Game) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        items(games, key = { it.id }) { game -> GameCard(game) { onSelect(game) } }
        item(span = { GridItemSpan(maxLineSpan) }) { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun NoBacklogMatches(query: String, modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Outlined.Search, null, tint = TextMuted, modifier = Modifier.size(44.dp))
        Text("No games found", color = TextPrimary, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 14.dp))
        Text("Nothing on your shelf matches “$query”.", color = TextMuted, fontSize = 13.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BacklogSortSheet(selected: BacklogSort, onClose: () -> Unit, onSelect: (BacklogSort) -> Unit) {
    ShelvedSheet("Sort backlog", onClose) {
        BacklogSort.entries.forEach { option ->
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                    .clickable { onSelect(option) }.padding(horizontal = 14.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    option.label,
                    color = if (option == selected) Accent else TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = if (option == selected) FontWeight.SemiBold else FontWeight.Normal,
                    modifier = Modifier.weight(1f),
                )
                if (option == selected) Icon(Icons.Default.Check, null, tint = Accent, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddGameSheet(
    state: SearchUiState,
    onClose: () -> Unit,
    onQueryChanged: (String) -> Unit,
    onGameSelected: (Game?) -> Unit,
    onAdd: (Game) -> Unit,
) {
    ShelvedSheet("Add Game", onClose) {
        SearchField(state.query, onChange = onQueryChanged)
        when (val status = state.status) {
            SearchStatus.Loading -> CircularProgressIndicator(
                color = Accent,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(24.dp),
            )
            is SearchStatus.Error -> Text(status.message, color = TextMuted, modifier = Modifier.padding(vertical = 18.dp))
            else -> Unit
        }
        LazyColumn(Modifier.fillMaxWidth()) {
            items(state.results, key = { it.id }) { game ->
                SearchResultRow(game) { onGameSelected(game) }
            }
        }
    }
    state.selectedGame?.let { game ->
        CatalogGameSheet(game, onClose = { onGameSelected(null) }, onAdd = onAdd)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditGameSheet(game: Game, onClose: () -> Unit, onSave: (Game) -> Unit) {
    var status by remember { mutableStateOf(game.status) }
    var hours by remember { mutableStateOf(game.hoursPlayed?.toString().orEmpty()) }
    var notes by remember { mutableStateOf(game.notes.orEmpty()) }
    ShelvedSheet("Game Details", onClose) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(72.dp).clip(RoundedCornerShape(10.dp)).background(SurfaceElevated)) {
                game.coverImageUrl?.let { AsyncImage(it, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop) }
            }
            Text(game.name, color = TextPrimary, fontSize = 19.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 14.dp))
        }
        SectionLabel("Status")
        StatusPicker(status) { status = it }
        SectionLabel("Hours played")
        ShelvedField(hours, { hours = it.filter(Char::isDigit) }, "0", KeyboardType.Number)
        SectionLabel("Notes")
        ShelvedField(notes, { notes = it }, "What do you think so far?", minLines = 3)
        PrimaryButton("Save") {
            onSave(game.copy(status = status, hoursPlayed = hours.toIntOrNull(), notes = notes.trim().ifBlank { null }))
        }
    }
}
