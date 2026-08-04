package com.paulrod.shelved.data

import com.paulrod.shelved.data.model.Game
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InMemoryLibraryRepositoryTest {
    @Test
    fun aNewTrialRepositoryAlwaysStartsEmpty() {
        val firstSession = InMemoryLibraryRepository().apply {
            addGame(Game("trial", "Trial game"))
        }

        val nextSession = InMemoryLibraryRepository()

        assertEquals(listOf("trial"), firstSession.games.value.map(Game::id))
        assertTrue(nextSession.games.value.isEmpty())
    }
}
