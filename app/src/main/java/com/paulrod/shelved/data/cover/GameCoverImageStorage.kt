package com.paulrod.shelved.data.cover

import com.paulrod.shelved.data.image.LocalImageSource

data class CoverCropRequest(
    val source: LocalImageSource,
    val zoom: Float,
    val horizontalOffset: Float,
    val verticalOffset: Float,
)

interface GameCoverImageStorage {
    suspend fun save(request: CoverCropRequest): String
    suspend fun remove(path: String?)
    suspend fun prune(referencedPaths: Set<String>)
}
