package com.paulrod.shelved.ui.backlog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ZoomIn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.paulrod.shelved.R
import com.paulrod.shelved.data.cover.COVER_ASPECT_RATIO
import com.paulrod.shelved.data.cover.CoverCropRequest
import com.paulrod.shelved.data.cover.MAX_COVER_ZOOM
import com.paulrod.shelved.data.image.LocalImageSource
import com.paulrod.shelved.ui.components.PrimaryButton
import com.paulrod.shelved.ui.components.ShelvedSheet
import com.paulrod.shelved.ui.theme.Accent
import com.paulrod.shelved.ui.theme.SurfaceElevated
import com.paulrod.shelved.ui.theme.TextMuted

@Composable
internal fun GameCoverCropSheet(
    source: LocalImageSource,
    onClose: () -> Unit,
    onUseCover: (CoverCropRequest) -> Unit,
) {
    var zoom by remember(source) { mutableFloatStateOf(1f) }
    var offset by remember(source) { mutableStateOf(Offset.Zero) }
    var viewportSize by remember(source) { mutableStateOf(IntSize.Zero) }
    var imageSize by remember(source) { mutableStateOf(Size.Zero) }
    var hasError by remember(source) { mutableStateOf(false) }

    fun updateTransform(newZoom: Float, pan: Offset = Offset.Zero) {
        val safeZoom = newZoom.coerceIn(1f, MAX_COVER_ZOOM)
        val limit = coverTranslationLimit(viewportSize, imageSize, safeZoom)
        zoom = safeZoom
        offset = (offset + pan).coerceWithin(limit)
    }

    ShelvedSheet(stringResource(R.string.cover_crop_title), onClose) {
        Text(
            stringResource(R.string.cover_crop_instructions),
            color = TextMuted,
            fontSize = 13.sp,
            modifier = Modifier.padding(bottom = 14.dp),
        )
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(COVER_ASPECT_RATIO)
                .clip(RoundedCornerShape(18.dp))
                .background(SurfaceElevated)
                .onSizeChanged {
                    viewportSize = it
                    updateTransform(zoom)
                },
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = source.uri,
                contentDescription = stringResource(R.string.cover_crop_description),
                contentScale = ContentScale.Crop,
                onSuccess = { result ->
                    val intrinsicSize = result.painter.intrinsicSize
                    if (intrinsicSize.width > 0f && intrinsicSize.height > 0f) {
                        imageSize = intrinsicSize
                        hasError = false
                        updateTransform(zoom)
                    }
                },
                onError = { hasError = true },
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = zoom
                        scaleY = zoom
                        translationX = offset.x
                        translationY = offset.y
                    }
                    .pointerInput(source, viewportSize, imageSize) {
                        detectTransformGestures { _, pan, gestureZoom, _ ->
                            updateTransform(zoom * gestureZoom, pan)
                        }
                    },
            )
            if (imageSize == Size.Zero && !hasError) {
                CircularProgressIndicator(color = Accent, modifier = Modifier.size(32.dp))
            }
            Box(
                Modifier.fillMaxSize().border(2.dp, Accent.copy(alpha = .75f), RoundedCornerShape(18.dp)),
            )
        }
        if (hasError) {
            Text(
                stringResource(R.string.cover_crop_open_error),
                color = androidx.compose.ui.graphics.Color(0xFFFF8A80),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
        Row(
            Modifier.fillMaxWidth().padding(top = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.ZoomIn, null, tint = TextMuted, modifier = Modifier.size(20.dp))
            Slider(
                value = zoom,
                onValueChange = { updateTransform(it) },
                valueRange = 1f..MAX_COVER_ZOOM,
                modifier = Modifier.weight(1f),
            )
        }
        PrimaryButton(stringResource(R.string.cover_crop_confirm), enabled = imageSize != Size.Zero && !hasError) {
            val limit = coverTranslationLimit(viewportSize, imageSize, zoom)
            onUseCover(
                CoverCropRequest(
                    source = source,
                    zoom = zoom,
                    horizontalOffset = offset.x.normalizedBy(limit.x),
                    verticalOffset = offset.y.normalizedBy(limit.y),
                ),
            )
        }
    }
}

internal fun coverTranslationLimit(viewport: IntSize, image: Size, zoom: Float): Offset {
    if (viewport.width <= 0 || viewport.height <= 0 || image.width <= 0f || image.height <= 0f) {
        return Offset.Zero
    }
    val baseScale = maxOf(viewport.width / image.width, viewport.height / image.height)
    val renderedWidth = image.width * baseScale * zoom
    val renderedHeight = image.height * baseScale * zoom
    return Offset(
        x = ((renderedWidth - viewport.width) / 2f).coerceAtLeast(0f),
        y = ((renderedHeight - viewport.height) / 2f).coerceAtLeast(0f),
    )
}

private fun Offset.coerceWithin(limit: Offset): Offset = Offset(
    x = x.coerceIn(-limit.x, limit.x),
    y = y.coerceIn(-limit.y, limit.y),
)

private fun Float.normalizedBy(limit: Float): Float = if (limit > 0f) {
    (this / limit).coerceIn(-1f, 1f)
} else {
    0f
}
