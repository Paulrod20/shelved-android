package com.paulrod.shelved.data.image

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.exifinterface.media.ExifInterface

internal class LocalImageDecoder(context: Context) {
    private val contentResolver: ContentResolver = context.contentResolver

    fun decode(
        source: LocalImageSource,
        targetLongEdge: Int,
        maxSourceBytes: Long,
    ): Bitmap {
        val uri = source.uri.toUri()
        validateSourceSize(uri, maxSourceBytes)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            decodeWithImageDecoder(uri, targetLongEdge)
        } else {
            decodeWithBitmapFactory(uri, targetLongEdge)
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun decodeWithImageDecoder(source: Uri, targetLongEdge: Int): Bitmap {
        return ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, source)) { decoder, info, _ ->
            val sourceWidth = info.size.width
            val sourceHeight = info.size.height
            val longestEdge = maxOf(sourceWidth, sourceHeight)
            if (longestEdge > targetLongEdge) {
                val scale = targetLongEdge.toFloat() / longestEdge
                decoder.setTargetSize(
                    (sourceWidth * scale).toInt().coerceAtLeast(1),
                    (sourceHeight * scale).toInt().coerceAtLeast(1),
                )
            }
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }
    }

    private fun decodeWithBitmapFactory(source: Uri, targetLongEdge: Int): Bitmap {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        contentResolver.openInputStream(source)?.use { input ->
            BitmapFactory.decodeStream(input, null, bounds)
        } ?: error("Could not open the selected image.")
        check(bounds.outWidth > 0 && bounds.outHeight > 0) {
            "The selected file is not a supported image."
        }

        val options = BitmapFactory.Options().apply {
            inSampleSize = calculateImageSampleSize(bounds.outWidth, bounds.outHeight, targetLongEdge)
        }
        val bitmap = contentResolver.openInputStream(source)?.use { input ->
            BitmapFactory.decodeStream(input, null, options)
        } ?: error("Could not decode the selected image.")
        return bitmap.applyExifOrientation(readExifOrientation(source))
    }

    private fun validateSourceSize(source: Uri, maxSourceBytes: Long) {
        val length = runCatching {
            contentResolver.openAssetFileDescriptor(source, "r")?.use { it.length }
        }.getOrNull() ?: return
        check(length < 0 || length <= maxSourceBytes) {
            "The selected image is too large."
        }
    }

    private fun readExifOrientation(source: Uri): Int = runCatching {
        contentResolver.openFileDescriptor(source, "r")?.use { descriptor ->
            ExifInterface(descriptor.fileDescriptor).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL,
            )
        } ?: ExifInterface.ORIENTATION_NORMAL
    }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)

    private fun Bitmap.applyExifOrientation(orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.setScale(-1f, 1f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> {
                matrix.setRotate(180f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.setRotate(90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90f)
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.setRotate(-90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(-90f)
            else -> return this
        }

        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true).also { oriented ->
            if (oriented !== this) recycle()
        }
    }
}

internal fun calculateImageSampleSize(width: Int, height: Int, targetLongEdge: Int): Int {
    var sampleSize = 1
    while (maxOf(width, height) / (sampleSize * 2) >= targetLongEdge) {
        sampleSize *= 2
    }
    return sampleSize
}
