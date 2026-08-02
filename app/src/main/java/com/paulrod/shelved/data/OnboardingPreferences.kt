package com.paulrod.shelved.data

import android.content.Context
import androidx.core.content.edit

interface OnboardingCompletionStore {
    val isCompleted: Boolean
    fun complete()
}

class OnboardingPreferences(context: Context) : OnboardingCompletionStore {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override val isCompleted: Boolean
        get() = preferences.getBoolean(COMPLETED_KEY, false)

    override fun complete() {
        preferences.edit { putBoolean(COMPLETED_KEY, true) }
    }

    private companion object {
        const val PREFERENCES_NAME = "shelved"
        const val COMPLETED_KEY = "onboarding_completed"
    }
}
