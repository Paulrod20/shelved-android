package com.paulrod.shelved.data.profile

import com.paulrod.shelved.data.image.LocalImageSource

interface ProfileImageStorage {
    suspend fun save(source: LocalImageSource): String
    suspend fun remove(path: String?)
    suspend fun prune(referencedPaths: Set<String>)
}
