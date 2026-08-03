package com.paulrod.shelved.data.sync

interface CloudLibraryStore {
    suspend fun load(userId: String): LibrarySnapshot?
    suspend fun apply(userId: String, changes: LibraryChanges)
}
