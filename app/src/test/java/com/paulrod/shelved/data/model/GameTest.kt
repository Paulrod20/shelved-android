package com.paulrod.shelved.data.model

import org.junit.Assert.assertTrue
import org.junit.Test

class GameTest {
    @Test
    fun ratingMustBeBetweenOneAndFive() {
        assertTrue(runCatching { Game("low", "Low", rating = 0) }.isFailure)
        assertTrue(runCatching { Game("high", "High", rating = 6) }.isFailure)
        assertTrue(runCatching { Game("valid", "Valid", rating = 5) }.isSuccess)
        assertTrue(runCatching { Game("unrated", "Unrated") }.isSuccess)
    }
}
