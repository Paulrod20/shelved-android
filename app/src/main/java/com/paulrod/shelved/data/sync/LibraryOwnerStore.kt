package com.paulrod.shelved.data.sync

import android.content.Context
import androidx.core.content.edit

interface LibraryOwnerStore {
    var userId: String?
}

class SharedPreferencesLibraryOwnerStore(context: Context) : LibraryOwnerStore {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override var userId: String?
        get() = preferences.getString(USER_ID_KEY, null)
        set(value) {
            preferences.edit { putString(USER_ID_KEY, value) }
        }

    private companion object {
        const val PREFERENCES_NAME = "shelved_sync"
        const val USER_ID_KEY = "local_library_owner_user_id"
    }
}
