package com.paulrod.shelved.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paulrod.shelved.R
import com.paulrod.shelved.data.model.Game
import com.paulrod.shelved.data.model.Platform
import com.paulrod.shelved.data.model.Profile
import com.paulrod.shelved.ui.components.FilterPill
import com.paulrod.shelved.ui.components.GameCover
import com.paulrod.shelved.ui.components.PrimaryButton
import com.paulrod.shelved.ui.components.SectionLabel
import com.paulrod.shelved.ui.components.ShelvedField
import com.paulrod.shelved.ui.components.ShelvedSheet
import com.paulrod.shelved.ui.theme.Accent
import com.paulrod.shelved.ui.theme.AccentText
import com.paulrod.shelved.ui.theme.TextMuted

@Composable
internal fun EditProfileSheet(
    profile: Profile,
    games: List<Game>,
    isImageLoading: Boolean,
    hasImageError: Boolean,
    onImageSelected: (Uri) -> Unit,
    onImageRemoved: () -> Unit,
    onClose: () -> Unit,
    onSave: (Profile) -> Unit,
) {
    var name by remember { mutableStateOf(profile.displayName) }
    var bio by remember { mutableStateOf(profile.bio) }
    var platforms by remember { mutableStateOf(profile.favoritePlatforms) }
    var favorites by remember { mutableStateOf(profile.favoriteGameIds) }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let(onImageSelected)
    }

    ShelvedSheet("Edit Profile", onClose) {
        SectionLabel(stringResource(R.string.profile_picture_section))
        ProfileImageEditor(
            profile = profile,
            isLoading = isImageLoading,
            hasError = hasImageError,
            onChoose = {
                photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            onRemove = onImageRemoved,
        )
        SectionLabel("Display name")
        ShelvedField(name, { name = it }, "Your name")
        SectionLabel("Bio")
        ShelvedField(bio, { bio = it }, "A little about your gaming taste…", minLines = 3)
        SectionLabel("Favorite platforms")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(Platform.entries) { item ->
                FilterPill(item.label, item in platforms) {
                    platforms = if (item in platforms) platforms - item else platforms + item
                }
            }
        }
        SectionLabel("Favorite games · ${favorites.size}/6")
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
                                favorites.size < 6 -> favorites + game.id
                                else -> favorites
                            }
                        },
                    )
                }
            }
        }
        PrimaryButton("Save", enabled = !isImageLoading) {
            onSave(
                Profile(
                    displayName = name.trim(),
                    bio = bio.trim(),
                    profileImagePath = profile.profileImagePath,
                    favoritePlatforms = platforms,
                    favoriteGameIds = favorites,
                ),
            )
        }
    }
}

@Composable
private fun ProfileImageEditor(
    profile: Profile,
    isLoading: Boolean,
    hasError: Boolean,
    onChoose: () -> Unit,
    onRemove: () -> Unit,
) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        ProfileAvatar(profile, size = 104.dp)
        Spacer(Modifier.height(8.dp))
        if (isLoading) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(Modifier.size(18.dp), color = Accent, strokeWidth = 2.dp)
                Text(
                    stringResource(R.string.profile_picture_loading),
                    color = TextMuted,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        } else {
            Row(horizontalArrangement = Arrangement.Center) {
                TextButton(onClick = onChoose) {
                    Text(
                        stringResource(
                            if (profile.profileImagePath == null) {
                                R.string.profile_picture_choose
                            } else {
                                R.string.profile_picture_change
                            },
                        ),
                        color = Accent,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                if (profile.profileImagePath != null) {
                    TextButton(onClick = onRemove) {
                        Text(stringResource(R.string.profile_picture_remove), color = TextMuted)
                    }
                }
            }
        }
        if (hasError) {
            Text(
                stringResource(R.string.profile_picture_error),
                color = androidx.compose.ui.graphics.Color(0xFFFF8A80),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
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
            ) {
                Icon(Icons.Default.Check, null, tint = AccentText, modifier = Modifier.size(16.dp))
            }
        }
    }
}
