package com.paulrod.shelved.data.image

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ManagedImageDirectoryTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun pruneKeepsReferencedFileAndDeletesOnlyManagedOrphans() {
        val managedFolder = temporaryFolder.newFolder("managed")
        val outsideFolder = temporaryFolder.newFolder("outside")
        val managedImages = ManagedImageDirectory.forTesting(managedFolder)
        val referenced = File(managedFolder, "referenced.jpg").apply { createNewFile() }
        val orphan = File(managedFolder, "orphan.jpg").apply { createNewFile() }
        val outside = File(outsideFolder, "outside.jpg").apply { createNewFile() }

        managedImages.prune(setOf(referenced.path, outside.path))

        assertTrue(referenced.exists())
        assertFalse(orphan.exists())
        assertTrue(outside.exists())
    }

    @Test
    fun removeCannotDeleteFileOutsideManagedDirectory() {
        val managedFolder = temporaryFolder.newFolder("managed")
        val outside = temporaryFolder.newFile("outside.jpg")
        val managedImages = ManagedImageDirectory.forTesting(managedFolder)

        managedImages.remove(outside.path)

        assertTrue(outside.exists())
    }
}
