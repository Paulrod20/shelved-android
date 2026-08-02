package com.paulrod.shelved.data.cover

import android.content.Context
import android.graphics.Bitmap
import androidx.core.graphics.scale
import com.paulrod.shelved.data.image.LocalImageDecoder
import com.paulrod.shelved.data.image.ManagedImageDirectory
import java.io.FileOutputStream
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GameCoverImageStore(context: Context) : GameCoverImageStorage {
    private val decoder = LocalImageDecoder(context)
    private val managedImages = ManagedImageDirectory(context, DIRECTORY_NAME)

    override suspend fun save(request: CoverCropRequest): String = withContext(Dispatchers.IO) {
        val decoded = decoder.decode(
            source = request.source,
            targetLongEdge = calculateCoverDecodeTarget(request.zoom),
            maxSourceBytes = MAX_SOURCE_BYTES,
        )
        val bounds = calculateCoverCropBounds(
            sourceWidth = decoded.width,
            sourceHeight = decoded.height,
            zoom = request.zoom,
            horizontalOffset = request.horizontalOffset,
            verticalOffset = request.verticalOffset,
        )
        val cropped = Bitmap.createBitmap(
            decoded,
            bounds.left,
            bounds.top,
            bounds.width,
            bounds.height,
        )
        val resized = cropped.scale(COVER_OUTPUT_WIDTH, COVER_OUTPUT_HEIGHT)
        val destination = managedImages.newJpeg("cover_")

        try {
            FileOutputStream(destination).use { output ->
                check(resized.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)) {
                    "Could not save the selected cover art."
                }
            }
        } catch (error: Throwable) {
            destination.delete()
            throw error
        } finally {
            if (resized !== cropped) resized.recycle()
            if (cropped !== decoded) cropped.recycle()
            decoded.recycle()
        }

        destination.absolutePath
    }

    override suspend fun remove(path: String?) = withContext(Dispatchers.IO) {
        managedImages.remove(path)
    }

    override suspend fun prune(referencedPaths: Set<String>) = withContext(Dispatchers.IO) {
        managedImages.prune(referencedPaths)
    }

    private companion object {
        const val DIRECTORY_NAME = "game_covers"
        const val JPEG_QUALITY = 90
        const val MAX_SOURCE_BYTES = 50L * 1024 * 1024
    }
}

internal data class CoverCropBounds(
    val left: Int,
    val top: Int,
    val width: Int,
    val height: Int,
)

internal fun calculateCoverCropBounds(
    sourceWidth: Int,
    sourceHeight: Int,
    zoom: Float,
    horizontalOffset: Float,
    verticalOffset: Float,
): CoverCropBounds {
    require(sourceWidth > 0 && sourceHeight > 0)
    val safeZoom = zoom.coerceIn(1f, MAX_COVER_ZOOM)
    val sourceRatio = sourceWidth.toFloat() / sourceHeight
    val (baseWidth, baseHeight) = if (sourceRatio > COVER_ASPECT_RATIO) {
        sourceHeight * COVER_ASPECT_RATIO to sourceHeight.toFloat()
    } else {
        sourceWidth.toFloat() to sourceWidth / COVER_ASPECT_RATIO
    }
    val cropWidth = (baseWidth / safeZoom).roundToInt().coerceIn(1, sourceWidth)
    val cropHeight = (baseHeight / safeZoom).roundToInt().coerceIn(1, sourceHeight)
    val maxHorizontalTravel = (sourceWidth - cropWidth) / 2f
    val maxVerticalTravel = (sourceHeight - cropHeight) / 2f
    val centerX = sourceWidth / 2f - horizontalOffset.coerceIn(-1f, 1f) * maxHorizontalTravel
    val centerY = sourceHeight / 2f - verticalOffset.coerceIn(-1f, 1f) * maxVerticalTravel
    val left = (centerX - cropWidth / 2f).roundToInt().coerceIn(0, sourceWidth - cropWidth)
    val top = (centerY - cropHeight / 2f).roundToInt().coerceIn(0, sourceHeight - cropHeight)
    return CoverCropBounds(left, top, cropWidth, cropHeight)
}

internal const val COVER_ASPECT_RATIO = 2f / 3f
internal const val MAX_COVER_ZOOM = 4f
internal const val COVER_OUTPUT_WIDTH = 1200
internal const val COVER_OUTPUT_HEIGHT = 1800
private const val MAX_COVER_DECODE_EDGE = 4096

internal fun calculateCoverDecodeTarget(zoom: Float): Int =
    (COVER_OUTPUT_HEIGHT * zoom.coerceIn(1f, MAX_COVER_ZOOM))
        .roundToInt()
        .coerceAtMost(MAX_COVER_DECODE_EDGE)
