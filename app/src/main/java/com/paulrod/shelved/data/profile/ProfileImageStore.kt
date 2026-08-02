package com.paulrod.shelved.data.profile

import android.content.Context
import android.graphics.Bitmap
import androidx.core.graphics.scale
import com.paulrod.shelved.data.image.LocalImageDecoder
import com.paulrod.shelved.data.image.LocalImageSource
import com.paulrod.shelved.data.image.ManagedImageDirectory
import com.paulrod.shelved.data.image.calculateImageSampleSize
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProfileImageStore(context: Context) : ProfileImageStorage {
    private val decoder = LocalImageDecoder(context)
    private val managedImages = ManagedImageDirectory(context, DIRECTORY_NAME)

    override suspend fun save(source: LocalImageSource): String = withContext(Dispatchers.IO) {
        val decoded = decoder.decode(source, PROFILE_DECODE_TARGET, MAX_SOURCE_BYTES)
        val square = decoded.centerCroppedSquare()
        val resized = if (square.width == OUTPUT_SIZE && square.height == OUTPUT_SIZE) {
            square
        } else {
            square.scale(OUTPUT_SIZE, OUTPUT_SIZE)
        }
        val destination = managedImages.newJpeg("avatar_")

        try {
            FileOutputStream(destination).use { output ->
                check(resized.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)) {
                    "Could not save the selected profile picture."
                }
            }
        } catch (error: Throwable) {
            destination.delete()
            throw error
        } finally {
            if (resized !== square) resized.recycle()
            if (square !== decoded) square.recycle()
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

    private fun Bitmap.centerCroppedSquare(): Bitmap {
        val side = minOf(width, height)
        return if (width == height) this else Bitmap.createBitmap(
            this,
            (width - side) / 2,
            (height - side) / 2,
            side,
            side,
        )
    }

    private companion object {
        const val DIRECTORY_NAME = "profile_images"
        const val OUTPUT_SIZE = 512
        const val JPEG_QUALITY = 88
        const val MAX_SOURCE_BYTES = 20L * 1024 * 1024
    }
}

internal fun calculateProfileImageSampleSize(width: Int, height: Int): Int {
    return calculateImageSampleSize(width, height, PROFILE_DECODE_TARGET)
}

private const val PROFILE_DECODE_TARGET = 1024
