package com.paulrod.shelved.data.profile

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.core.graphics.scale
import androidx.core.net.toUri
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProfileImageStore(context: Context) : ProfileImageStorage {
    private val contentResolver: ContentResolver = context.contentResolver
    private val imageDirectory = File(context.filesDir, DIRECTORY_NAME)

    override suspend fun save(source: ProfileImageSource): String = withContext(Dispatchers.IO) {
        imageDirectory.mkdirs()
        val decoded = decodeSampledBitmap(source.uri.toUri())
        val square = decoded.centerCroppedSquare()
        val resized = if (square.width == OUTPUT_SIZE && square.height == OUTPUT_SIZE) {
            square
        } else {
            square.scale(OUTPUT_SIZE, OUTPUT_SIZE)
        }
        val destination = File(imageDirectory, "avatar_${UUID.randomUUID()}.jpg")

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
        deleteManagedFile(path)
    }

    private fun decodeSampledBitmap(source: Uri): Bitmap {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        val boundsStream = contentResolver.openInputStream(source)
            ?: error("Could not open the selected image.")
        boundsStream.use { BitmapFactory.decodeStream(it, null, bounds) }
        check(bounds.outWidth > 0 && bounds.outHeight > 0) { "The selected file is not a supported image." }

        val options = BitmapFactory.Options().apply {
            inSampleSize = calculateProfileImageSampleSize(bounds.outWidth, bounds.outHeight)
        }
        val bitmap = contentResolver.openInputStream(source)?.use { input ->
            BitmapFactory.decodeStream(input, null, options)
        } ?: error("Could not decode the selected image.")
        return bitmap.applyExifOrientation(readExifOrientation(source))
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

    private fun deleteManagedFile(path: String?) {
        val file = path?.let(::File) ?: return
        val managedDirectory = imageDirectory.canonicalFile
        if (file.canonicalFile.parentFile == managedDirectory) file.delete()
    }

    private companion object {
        const val DIRECTORY_NAME = "profile_images"
        const val OUTPUT_SIZE = 512
        const val JPEG_QUALITY = 88
    }
}

internal fun calculateProfileImageSampleSize(width: Int, height: Int): Int {
    var sampleSize = 1
    while (maxOf(width, height) / (sampleSize * 2) >= PROFILE_DECODE_TARGET) {
        sampleSize *= 2
    }
    return sampleSize
}

private const val PROFILE_DECODE_TARGET = 1024
