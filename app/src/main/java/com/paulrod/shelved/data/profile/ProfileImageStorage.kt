package com.paulrod.shelved.data.profile

@JvmInline
value class ProfileImageSource(val uri: String)

interface ProfileImageStorage {
    suspend fun save(source: ProfileImageSource): String
    suspend fun remove(path: String?)
}
