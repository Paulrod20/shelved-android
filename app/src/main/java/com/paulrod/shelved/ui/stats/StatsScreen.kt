package com.paulrod.shelved.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paulrod.shelved.data.model.GameStatus
import com.paulrod.shelved.ui.components.EmptyLibrary
import com.paulrod.shelved.ui.components.SectionLabel
import com.paulrod.shelved.ui.components.ShelvedScreen
import com.paulrod.shelved.ui.components.label
import com.paulrod.shelved.ui.theme.Accent
import com.paulrod.shelved.ui.theme.Surface
import com.paulrod.shelved.ui.theme.TextMuted
import com.paulrod.shelved.ui.theme.TextPrimary

@Composable
fun StatsScreen(state: StatsUiState) {
    ShelvedScreen("Stats") {
        if (state.isEmpty) {
            EmptyLibrary(true, Modifier.fillMaxSize())
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard("Games", state.totalGames.toString(), Modifier.weight(1f))
                        StatCard("Hours", state.totalHours.toString(), Modifier.weight(1f))
                    }
                }
                item { SectionLabel("Library breakdown") }
                items(GameStatus.entries) { status ->
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Surface).padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(status.label, color = TextPrimary, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                        Text(
                            state.statusCounts[status].orZero().toString(),
                            color = Accent,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier) {
    androidx.compose.foundation.layout.Column(
        modifier.clip(RoundedCornerShape(18.dp)).background(Surface).padding(18.dp),
    ) {
        Text(value, color = Accent, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Text(label, color = TextMuted, fontSize = 13.sp)
    }
}

private fun Int?.orZero(): Int = this ?: 0
