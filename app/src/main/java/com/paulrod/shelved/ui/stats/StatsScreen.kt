package com.paulrod.shelved.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.paulrod.shelved.ui.components.EmptyLibrary
import com.paulrod.shelved.ui.components.SectionLabel
import com.paulrod.shelved.ui.components.ShelvedScreen

@Composable
fun StatsScreen(state: StatsUiState) {
    ShelvedScreen("Stats") {
        if (state.isEmpty) {
            EmptyLibrary(true, Modifier.fillMaxSize())
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item { StatsSummaryCard(state) }

                item { SectionLabel("Library progress") }
                item { LibraryProgressCard(state) }

                item { SectionLabel("Your playtime") }
                item { PlaytimeInsights(state) }

                if (state.mostPlayedGames.isNotEmpty()) {
                    item { SectionLabel("Most played") }
                    itemsIndexed(
                        items = state.mostPlayedGames,
                        key = { _, game -> game.id },
                    ) { index, game ->
                        MostPlayedGameRow(rank = index + 1, game = game)
                    }
                }

                item { Spacer(Modifier.height(12.dp)) }
            }
        }
    }
}
