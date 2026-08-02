package com.paulrod.shelved.data.profile

import org.junit.Assert.assertEquals
import org.junit.Test

class ProfileImageStoreTest {
    @Test
    fun smallImagesDecodeAtOriginalResolution() {
        assertEquals(1, calculateProfileImageSampleSize(800, 600))
    }

    @Test
    fun largeLandscapeAndPortraitImagesAreSampledByLongestEdge() {
        assertEquals(4, calculateProfileImageSampleSize(6000, 1000))
        assertEquals(4, calculateProfileImageSampleSize(1000, 6000))
    }
}
