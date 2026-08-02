package com.paulrod.shelved.data.cover

import org.junit.Assert.assertEquals
import org.junit.Test

class GameCoverCropTest {
    @Test
    fun landscapeImageGetsCenteredTwoByThreeCrop() {
        val crop = calculateCoverCropBounds(
            sourceWidth = 3000,
            sourceHeight = 2000,
            zoom = 1f,
            horizontalOffset = 0f,
            verticalOffset = 0f,
        )

        assertEquals(1333, crop.width)
        assertEquals(2000, crop.height)
        assertEquals(834, crop.left)
        assertEquals(0, crop.top)
    }

    @Test
    fun zoomAndOffsetsRemainInsideSourceImage() {
        val topLeft = calculateCoverCropBounds(
            sourceWidth = 1200,
            sourceHeight = 1800,
            zoom = 2f,
            horizontalOffset = 1f,
            verticalOffset = 1f,
        )
        val bottomRight = calculateCoverCropBounds(
            sourceWidth = 1200,
            sourceHeight = 1800,
            zoom = 2f,
            horizontalOffset = -1f,
            verticalOffset = -1f,
        )

        assertEquals(CoverCropBounds(0, 0, 600, 900), topLeft)
        assertEquals(CoverCropBounds(600, 900, 600, 900), bottomRight)
    }

    @Test
    fun zoomIsClampedToSupportedRange() {
        val overZoomed = calculateCoverCropBounds(600, 900, 20f, 0f, 0f)

        assertEquals(150, overZoomed.width)
        assertEquals(225, overZoomed.height)
    }

    @Test
    fun decodeResolutionGrowsWithZoomButRemainsMemoryBounded() {
        assertEquals(1800, calculateCoverDecodeTarget(1f))
        assertEquals(3600, calculateCoverDecodeTarget(2f))
        assertEquals(4096, calculateCoverDecodeTarget(4f))
        assertEquals(4096, calculateCoverDecodeTarget(20f))
    }
}
