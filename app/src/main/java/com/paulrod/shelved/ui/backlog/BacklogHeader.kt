package com.paulrod.shelved.ui.backlog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.paulrod.shelved.ui.theme.Accent
import com.paulrod.shelved.ui.theme.AccentText
import com.paulrod.shelved.ui.theme.TextPrimary

@Composable
internal fun BacklogHeader(state: BacklogUiState, onAction: (BacklogAction) -> Unit) {
    if (state.selectedGameIds.isNotEmpty()) {
        SelectionActions(onAction)
    } else {
        DefaultActions(state, onAction)
    }
}

@Composable
private fun SelectionActions(onAction: (BacklogAction) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        HeaderAction(Icons.Default.Close, "Clear selection") {
            onAction(BacklogAction.SelectionCleared)
        }
        IconButton(
            onClick = { onAction(BacklogAction.DeleteRequested) },
            modifier = Modifier.size(40.dp),
        ) {
            Icon(
                Icons.Default.Delete,
                "Delete selected games",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(21.dp),
            )
        }
    }
}

@Composable
private fun DefaultActions(state: BacklogUiState, onAction: (BacklogAction) -> Unit) {
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
}

@Composable
private fun HeaderAction(
    icon: ImageVector,
    description: String,
    active: Boolean = false,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick, modifier = Modifier.size(40.dp)) {
        Icon(icon, description, tint = if (active) Accent else TextPrimary, modifier = Modifier.size(21.dp))
    }
}
