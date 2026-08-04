package com.paulrod.shelved.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Gamepad
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.paulrod.shelved.data.model.Game
import com.paulrod.shelved.ui.theme.Accent
import com.paulrod.shelved.ui.theme.AccentText
import com.paulrod.shelved.ui.theme.SurfaceElevated
import com.paulrod.shelved.ui.theme.TextMuted
import com.paulrod.shelved.ui.theme.TextPrimary
import java.io.File

@Composable
fun GameCard(
    game: Game,
    selected: Boolean = false,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit,
) {
    Column(Modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick)) {
        Box(Modifier.fillMaxWidth().height(148.dp)) {
            GameCover(game, Modifier.fillMaxSize())
            if (selected) SelectionOverlay()
        }
        Text(
            game.name,
            color = if (selected) Accent else TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth().padding(top = 7.dp),
        )
        game.rating?.let { rating ->
            StarRatingDisplay(
                rating = rating,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 3.dp),
            )
        }
    }
}

@Composable
fun GameCover(game: Game, modifier: Modifier = Modifier) {
    Box(
        modifier.clip(RoundedCornerShape(11.dp)).background(SurfaceElevated).height(148.dp),
        contentAlignment = Alignment.Center,
    ) {
        val model = game.coverModel()
        if (model != null) {
            val context = LocalContext.current
            val request = remember(context, model) {
                ImageRequest.Builder(context)
                    .data(model)
                    .crossfade(true)
                    .build()
            }
            AsyncImage(
                model = request,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Icon(Icons.Outlined.Gamepad, null, tint = TextMuted, modifier = Modifier.size(30.dp))
        }
    }
}

internal fun Game.coverModel(): Any? = customCoverImagePath?.let(::File) ?: coverImageUrl

@Composable
private fun BoxScope.SelectionOverlay() {
    Box(
        Modifier.fillMaxSize().clip(RoundedCornerShape(11.dp))
            .background(Accent.copy(alpha = .2f))
            .border(2.dp, Accent, RoundedCornerShape(11.dp)),
    )
    Box(
        Modifier.align(Alignment.TopEnd).padding(7.dp).size(24.dp)
            .clip(RoundedCornerShape(8.dp)).background(Accent),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Default.Check, "Selected", tint = AccentText, modifier = Modifier.size(17.dp))
    }
}
