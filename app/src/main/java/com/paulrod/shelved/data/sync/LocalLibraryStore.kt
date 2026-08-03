package com.paulrod.shelved.data.sync

import com.paulrod.shelved.data.model.Game
import com.paulrod.shelved.data.model.Profile
import kotlinx.coroutines.flow.StateFlow

interface LocalLibraryStore {
    val games: StateFlow<List<Game>>
    val profile: StateFlow<Profile>

    fun replaceLibrary(snapshot: LibrarySnapshot)
}
