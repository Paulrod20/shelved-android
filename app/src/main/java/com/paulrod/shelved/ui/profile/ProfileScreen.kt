package com.paulrod.shelved.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreHoriz
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paulrod.shelved.data.model.Game
import com.paulrod.shelved.data.model.Platform
import com.paulrod.shelved.data.model.Profile
import com.paulrod.shelved.ui.components.FilterPill
import com.paulrod.shelved.ui.components.GameCover
import com.paulrod.shelved.ui.components.PrimaryButton
import com.paulrod.shelved.ui.components.SectionLabel
import com.paulrod.shelved.ui.components.ShelvedField
import com.paulrod.shelved.ui.components.ShelvedScreen
import com.paulrod.shelved.ui.components.ShelvedSheet
import com.paulrod.shelved.ui.theme.Accent
import com.paulrod.shelved.ui.theme.AccentText
import com.paulrod.shelved.ui.theme.Surface
import com.paulrod.shelved.ui.theme.TextMuted
import com.paulrod.shelved.ui.theme.TextPrimary

@Composable
fun ProfileScreen(state: ProfileUiState, onAction: (ProfileAction) -> Unit) {
    ShelvedScreen("Profile", actions = {
        Row {
            HeaderButton(Icons.Default.Edit, "Edit") { onAction(ProfileAction.EditRequested) }
            Spacer(Modifier.width(8.dp))
            HeaderButton(Icons.Default.MoreHoriz, "Menu") { onAction(ProfileAction.MenuRequested) }
        }
    }) {
        LazyColumn(Modifier.fillMaxSize()) {
            item { ProfileIdentity(state.profile, state.games.size) }
            item { SectionLabel("Bio"); ProfileText(state.profile.bio, "Tap Edit to add a bio.") }
            item {
                SectionLabel("Favorite platform")
                ProfileText(state.profile.favoritePlatform?.label.orEmpty(), "No platform selected yet.")
            }
            item { SectionLabel("Favorite games") }
            if (state.favoriteGames.isEmpty()) {
                item { ProfileText("", "Pick up to three favorites in Edit.") }
            } else {
                item { FavoriteGames(state.favoriteGames) }
            }
        }
    }

    if (state.isEditSheetVisible) {
        EditProfileSheet(
            profile = state.profile,
            games = state.games,
            onClose = { onAction(ProfileAction.EditDismissed) },
            onSave = { onAction(ProfileAction.ProfileSaved(it)) },
        )
    }
    if (state.isMenuSheetVisible) {
        ShelvedSheet("Menu", { onAction(ProfileAction.MenuDismissed) }) {
            listOf("Settings", "About Shelved").forEach { item ->
                Text(
                    item,
                    color = TextPrimary,
                    fontSize = 16.sp,
                    modifier = Modifier.fillMaxWidth().clickable { onAction(ProfileAction.MenuDismissed) }
                        .padding(vertical = 16.dp),
                )
            }
        }
    }
}

@Composable
private fun HeaderButton(icon: androidx.compose.ui.graphics.vector.ImageVector, description: String, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(40.dp).clip(CircleShape).background(Surface),
    ) { Icon(icon, description, tint = TextPrimary, modifier = Modifier.size(18.dp)) }
}

@Composable
private fun ProfileIdentity(profile: Profile, gameCount: Int) {
    Column(
        Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier.size(90.dp).clip(CircleShape).background(Surface).border(2.dp, Accent, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                profile.displayName.firstOrNull()?.uppercase() ?: "🎮",
                color = TextPrimary,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Text(
            profile.displayName.ifBlank { "Add your name" },
            color = TextPrimary,
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 12.dp),
        )
        Text(
            "$gameCount ${if (gameCount == 1) "game" else "games"} shelved",
            color = TextMuted,
            fontSize = 13.sp,
        )
    }
}

@Composable
private fun ProfileText(value: String, placeholder: String) {
    Text(
        value.ifBlank { placeholder },
        color = if (value.isBlank()) TextMuted else TextPrimary,
        fontStyle = if (value.isBlank()) FontStyle.Italic else FontStyle.Normal,
        fontSize = 14.sp,
        modifier = Modifier.padding(bottom = 22.dp),
    )
}

@Composable
private fun FavoriteGames(games: List<Game>) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        games.forEach { game ->
            Column(Modifier.weight(1f)) {
                GameCover(game.coverImageUrl, Modifier.fillMaxWidth())
                Text(game.name, color = TextPrimary, fontSize = 11.sp, maxLines = 2)
            }
        }
        repeat(3 - games.size) { Spacer(Modifier.weight(1f)) }
    }
}

@Composable
private fun EditProfileSheet(
    profile: Profile,
    games: List<Game>,
    onClose: () -> Unit,
    onSave: (Profile) -> Unit,
) {
    var name by remember { mutableStateOf(profile.displayName) }
    var bio by remember { mutableStateOf(profile.bio) }
    var platform by remember { mutableStateOf(profile.favoritePlatform) }
    var favorites by remember { mutableStateOf(profile.favoriteGameIds) }
    ShelvedSheet("Edit Profile", onClose) {
        SectionLabel("Display name")
        ShelvedField(name, { name = it }, "Your name")
        SectionLabel("Bio")
        ShelvedField(bio, { bio = it }, "A little about your gaming taste…", minLines = 3)
        SectionLabel("Favorite platform")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(Platform.entries) { item -> FilterPill(item.label, item == platform) { platform = item } }
        }
        SectionLabel("Favorite games · ${favorites.size}/3")
        if (games.isEmpty()) {
            Text("Add games to your shelf first.", color = TextMuted)
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(games, key = { it.id }) { game ->
                    FavoriteGamePickerItem(
                        game = game,
                        selected = game.id in favorites,
                        onClick = {
                            favorites = when {
                                game.id in favorites -> favorites - game.id
                                favorites.size < 3 -> favorites + game.id
                                else -> favorites
                            }
                        },
                    )
                }
            }
        }
        PrimaryButton("Save") { onSave(Profile(name.trim(), bio.trim(), platform, favorites)) }
    }
}

@Composable
private fun FavoriteGamePickerItem(game: Game, selected: Boolean, onClick: () -> Unit) {
    Box(Modifier.width(88.dp).clickable(onClick = onClick)) {
        GameCover(game.coverImageUrl, Modifier.fillMaxWidth())
        if (selected) {
            Box(
                Modifier.align(Alignment.TopEnd).padding(6.dp).size(24.dp).clip(CircleShape).background(Accent),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Default.Check, null, tint = AccentText, modifier = Modifier.size(16.dp)) }
        }
    }
}
