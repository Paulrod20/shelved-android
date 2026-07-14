package com.paulrod.shelved.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.paulrod.shelved.ui.theme.Accent
import com.paulrod.shelved.ui.theme.AccentText
import com.paulrod.shelved.ui.theme.Surface
import com.paulrod.shelved.ui.theme.TextMuted

/** The four destinations deliberately match the existing Shelved app. */
enum class Destination(val label: String) {
    BACKLOG("Backlog"),
    SEARCH("Search"),
    STATS("Stats"),
    PROFILE("Profile"),
}

@Composable
fun ShelvedApp() {
    var selected by rememberSaveable { mutableStateOf(Destination.BACKLOG) }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = { PillTabBar(selected = selected, onSelect = { selected = it }) },
    ) { padding ->
        when (selected) {
            Destination.BACKLOG -> NativeScreen("SHELVED", "Your native Android game library will appear here.", Modifier.padding(padding))
            Destination.SEARCH -> NativeScreen("Search", "RAWG search will use a native Android network client.", Modifier.padding(padding))
            Destination.STATS -> NativeScreen("Stats", "Stats is ready for the native game database.", Modifier.padding(padding))
            Destination.PROFILE -> NativeScreen("Profile", "Profile will be stored with Android DataStore.", Modifier.padding(padding))
        }
    }
}

@Composable
private fun NativeScreen(title: String, message: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Text(
            text = title,
            modifier = Modifier.padding(top = 20.dp, bottom = 16.dp),
            style = MaterialTheme.typography.titleLarge,
        )
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = message, color = TextMuted, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun PillTabBar(selected: Destination, onSelect: (Destination) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Destination.entries.forEach { destination ->
            val active = destination == selected
            Text(
                text = destination.label,
                modifier = Modifier
                    .weight(1f)
                    .background(if (active) Accent else Surface, RoundedCornerShape(20.dp))
                    .clickable { onSelect(destination) }
                    .padding(vertical = 10.dp),
                color = if (active) AccentText else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
        }
    }
}
