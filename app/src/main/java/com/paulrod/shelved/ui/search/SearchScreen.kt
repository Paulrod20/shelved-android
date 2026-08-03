package com.paulrod.shelved.ui.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paulrod.shelved.ui.components.CatalogGameSheet
import com.paulrod.shelved.ui.components.SearchField
import com.paulrod.shelved.ui.components.SearchFailurePanel
import com.paulrod.shelved.ui.components.SearchResultRow
import com.paulrod.shelved.ui.components.ShelvedScreen
import com.paulrod.shelved.ui.theme.Accent
import com.paulrod.shelved.ui.theme.TextMuted
import com.paulrod.shelved.ui.theme.TextPrimary

@Composable
fun SearchScreen(
    state: SearchUiState,
    onAction: (SearchAction) -> Unit,
) {
    ShelvedScreen("Search") {
        SearchField(state.query) { onAction(SearchAction.QueryChanged(it)) }
        when (val status = state.status) {
            SearchStatus.Loading -> CircularProgressIndicator(
                color = Accent,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(24.dp),
            )
            is SearchStatus.Error -> SearchFailurePanel(
                failure = status.failure,
                onRetry = { onAction(SearchAction.RetryRequested) },
                modifier = Modifier.padding(top = 16.dp),
            )
            SearchStatus.Ready -> if (state.results.isEmpty()) {
                Text("No games found.", color = TextMuted, modifier = Modifier.padding(top = 16.dp))
            }
            SearchStatus.Idle -> SearchPrompt()
        }
        LazyColumn(Modifier.fillMaxSize()) {
            items(state.results, key = { it.id }) { game ->
                SearchResultRow(game) { onAction(SearchAction.GameSelected(game)) }
            }
        }
    }

    state.selectedGame?.let { game ->
        CatalogGameSheet(
            game = game,
            alreadyAdded = game.id in state.libraryGameIds,
            isDetailsLoading = state.isDetailsLoading,
            detailsError = state.detailsError,
            onClose = { onAction(SearchAction.GameDismissed) },
            onAdd = { onAction(SearchAction.GameAdded(it)) },
        )
    }
}

@Composable
private fun SearchPrompt() {
    Column(
        Modifier.fillMaxWidth().padding(top = 70.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(Icons.Outlined.Search, null, tint = TextMuted, modifier = Modifier.size(42.dp))
        Text(
            "Find your next game",
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 12.dp),
        )
        Text("Search the RAWG game database.", color = TextMuted, fontSize = 13.sp)
    }
}
