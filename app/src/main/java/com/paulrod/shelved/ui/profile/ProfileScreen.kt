package com.paulrod.shelved.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.paulrod.shelved.R
import com.paulrod.shelved.data.auth.AuthSession
import com.paulrod.shelved.data.image.LocalImageSource
import com.paulrod.shelved.data.model.Game
import com.paulrod.shelved.data.model.Platform
import com.paulrod.shelved.data.model.Profile
import com.paulrod.shelved.ui.components.GameCover
import com.paulrod.shelved.ui.components.SectionLabel
import com.paulrod.shelved.ui.components.ShelvedScreen
import com.paulrod.shelved.ui.components.ShelvedSheet
import com.paulrod.shelved.ui.theme.Accent
import com.paulrod.shelved.ui.theme.AccentText
import com.paulrod.shelved.ui.theme.TextMuted
import com.paulrod.shelved.ui.theme.TextPrimary

@Composable
fun ProfileScreen(
    state: ProfileUiState,
    accountSession: AuthSession,
    onAction: (ProfileAction) -> Unit,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit,
) {
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
                SectionLabel("Favorite platforms")
                FavoritePlatforms(state.profile.favoritePlatforms)
            }
            item { SectionLabel("Favorite games") }
            if (state.favoriteGames.isEmpty()) {
                item { ProfileText("", "Pick up to six favorites in Edit.") }
            } else {
                item { FavoriteGames(state.favoriteGames) }
            }
            item {
                SectionLabel(stringResource(R.string.account_section))
                ProfileAccountCard(accountSession, onSignIn, onSignOut)
            }
        }
    }

    if (state.isEditSheetVisible) {
        EditProfileSheet(
            profile = state.profile.copy(profileImagePath = state.editingProfileImagePath),
            games = state.games,
            isImageLoading = state.isProfileImageLoading,
            hasImageError = state.hasProfileImageError,
            onImageSelected = { onAction(ProfileAction.ProfileImageSelected(LocalImageSource(it.toString()))) },
            onImageRemoved = { onAction(ProfileAction.ProfileImageRemoved) },
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
        modifier = Modifier.size(40.dp),
    ) { Icon(icon, description, tint = TextPrimary, modifier = Modifier.size(18.dp)) }
}

@Composable
private fun ProfileIdentity(profile: Profile, gameCount: Int) {
    Column(
        Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ProfileAvatar(profile)
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
private fun FavoritePlatforms(platforms: List<Platform>) {
    if (platforms.isEmpty()) {
        ProfileText("", "No platforms selected yet.")
        return
    }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(bottom = 22.dp),
    ) {
        items(platforms) { platform ->
            Text(
                text = platform.label,
                color = AccentText,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(Accent)
                    .padding(horizontal = 13.dp, vertical = 7.dp),
            )
        }
    }
}

@Composable
private fun FavoriteGames(games: List<Game>) {
    Column(
        modifier = Modifier.padding(bottom = 22.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        games.take(6).chunked(3).forEach { rowGames ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                rowGames.forEach { game ->
                    Column(Modifier.weight(1f)) {
                        GameCover(game, Modifier.fillMaxWidth())
                        Text(
                            game.name,
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                        )
                    }
                }
                repeat(3 - rowGames.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}
