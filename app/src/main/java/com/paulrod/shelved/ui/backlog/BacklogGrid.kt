package com.paulrod.shelved.ui.backlog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.paulrod.shelved.data.model.Game
import com.paulrod.shelved.ui.components.GameCard

@Composable
internal fun BacklogGameGrid(
    games: List<Game>,
    selectedGameIds: Set<String>,
    onSelect: (Game) -> Unit,
    onLongPress: (Game) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        items(games, key = { it.id }) { game ->
            GameCard(
                game = game,
                selected = game.id in selectedGameIds,
                onLongClick = { onLongPress(game) },
                onClick = { onSelect(game) },
            )
        }
        item(span = { GridItemSpan(maxLineSpan) }) { Spacer(Modifier.height(8.dp)) }
    }
}
