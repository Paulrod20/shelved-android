package com.paulrod.shelved.data.image

import android.content.Context
import java.io.File
import java.util.UUID

internal class ManagedImageDirectory private constructor(
    private val directory: File,
) {
    constructor(context: Context, directoryName: String) : this(File(context.filesDir, directoryName))

    fun newJpeg(prefix: String): File {
        directory.mkdirs()
        return File(directory, "$prefix${UUID.randomUUID()}.jpg")
    }

    fun remove(path: String?) {
        managedFile(path)?.delete()
    }

    fun prune(referencedPaths: Set<String>) {
        val referencedFiles = referencedPaths.mapNotNull(::managedFile).mapTo(mutableSetOf()) { it.path }
        directory.listFiles().orEmpty()
            .filter(File::isFile)
            .mapNotNull { runCatching { it.canonicalFile }.getOrNull() }
            .filterNot { it.path in referencedFiles }
            .forEach(File::delete)
    }

    private fun managedFile(path: String?): File? {
        val file = path?.let(::File) ?: return null
        return runCatching {
            file.canonicalFile.takeIf { it.parentFile == directory.canonicalFile }
        }.getOrNull()
    }

    internal companion object {
        fun forTesting(directory: File) = ManagedImageDirectory(directory)
    }
}
