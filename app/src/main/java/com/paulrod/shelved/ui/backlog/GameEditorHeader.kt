package com.paulrod.shelved.ui.backlog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Gamepad
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.paulrod.shelved.R
import com.paulrod.shelved.data.model.Game
import com.paulrod.shelved.ui.components.coverModel
import com.paulrod.shelved.ui.theme.Accent
import com.paulrod.shelved.ui.theme.Surface
import com.paulrod.shelved.ui.theme.SurfaceElevated
import com.paulrod.shelved.ui.theme.TextMuted
import com.paulrod.shelved.ui.theme.TextPrimary

@Composable
internal fun GameEditorHeader(
    game: Game,
    isCoverLoading: Boolean,
    hasCoverError: Boolean,
    onChooseCover: () -> Unit,
    onUseOriginalCover: () -> Unit,
) {
    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier.size(width = 176.dp, height = 228.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(SurfaceElevated),
            contentAlignment = Alignment.Center,
        ) {
            val cover = game.coverModel()
            if (cover == null) {
                Icon(
                    Icons.Outlined.Gamepad,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(42.dp),
                )
            } else {
                AsyncImage(
                    model = cover,
                    contentDescription = stringResource(R.string.cover_art_description, game.name),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
            if (isCoverLoading) {
                Box(
                    Modifier.fillMaxSize().background(Surface.copy(alpha = .76f)),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = Accent, strokeWidth = 2.dp, modifier = Modifier.size(28.dp))
                }
            }
        }
        Text(
            game.name,
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp, start = 12.dp, end = 12.dp),
        )
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(enabled = !isCoverLoading, onClick = onChooseCover) {
                Text(
                    stringResource(
                        if (game.customCoverImagePath == null) R.string.cover_art_choose else R.string.cover_art_change,
                    ),
                    color = Accent,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            if (game.customCoverImagePath != null) {
                TextButton(enabled = !isCoverLoading, onClick = onUseOriginalCover) {
                    Text(stringResource(R.string.cover_art_use_original), color = TextMuted)
                }
            }
        }
        if (hasCoverError) {
            Text(
                stringResource(R.string.cover_art_save_error),
                color = androidx.compose.ui.graphics.Color(0xFFFF8A80),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp, bottom = 4.dp),
            )
        }
    }
}
