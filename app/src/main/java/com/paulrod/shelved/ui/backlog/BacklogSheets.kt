package com.paulrod.shelved.ui.backlog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.paulrod.shelved.data.model.Game
import com.paulrod.shelved.ui.components.CatalogGameSheet
import com.paulrod.shelved.ui.components.PrimaryButton
import com.paulrod.shelved.ui.components.SearchField
import com.paulrod.shelved.ui.components.SearchResultRow
import com.paulrod.shelved.ui.components.SectionLabel
import com.paulrod.shelved.ui.components.ShelvedField
import com.paulrod.shelved.ui.components.ShelvedSheet
import com.paulrod.shelved.ui.components.StatusPicker
import com.paulrod.shelved.ui.search.SearchStatus
import com.paulrod.shelved.ui.search.SearchUiState
import com.paulrod.shelved.ui.theme.Accent
import com.paulrod.shelved.ui.theme.SurfaceElevated
import com.paulrod.shelved.ui.theme.TextMuted
import com.paulrod.shelved.ui.theme.TextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BacklogSortSheet(selected: BacklogSort, onClose: () -> Unit, onSelect: (BacklogSort) -> Unit) {
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
internal fun AddGameSheet(
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
internal fun EditGameSheet(game: Game, onClose: () -> Unit, onSave: (Game) -> Unit) {
    var status by remember { mutableStateOf(game.status) }
    var hours by remember { mutableStateOf(game.hoursPlayed?.toString().orEmpty()) }
    var notes by remember { mutableStateOf(game.notes) }
    ShelvedSheet("Game Details", onClose) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(72.dp).clip(RoundedCornerShape(10.dp)).background(SurfaceElevated)) {
                game.coverImageUrl?.let { AsyncImage(it, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop) }
            }
            Text(
                game.name,
                color = TextPrimary,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 14.dp),
            )
        }
        SectionLabel("Status")
        StatusPicker(status) { status = it }
        SectionLabel("Hours played")
        ShelvedField(hours, { hours = it.filter(Char::isDigit) }, "0", KeyboardType.Number)
        GameNotesSection(notes = notes, onNotesChange = { notes = it })
        PrimaryButton("Save") {
            onSave(game.copy(status = status, hoursPlayed = hours.toIntOrNull(), notes = notes))
        }
    }
}
