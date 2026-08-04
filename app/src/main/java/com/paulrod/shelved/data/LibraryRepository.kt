package com.paulrod.shelved.data

import com.paulrod.shelved.data.profile.ProfileRepository
import com.paulrod.shelved.data.sync.LocalLibraryStore

/** Complete mutable library contract shared by persistent and trial stores. */
interface LibraryRepository : ShelvedDataRepository, ProfileRepository, LocalLibraryStore
