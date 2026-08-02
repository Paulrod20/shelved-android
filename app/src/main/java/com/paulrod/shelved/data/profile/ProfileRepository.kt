package com.paulrod.shelved.data.profile

import com.paulrod.shelved.data.model.Game
import com.paulrod.shelved.data.model.Profile
import kotlinx.coroutines.flow.StateFlow

interface ProfileRepository {
    val games: StateFlow<List<Game>>
    val profile: StateFlow<Profile>

    fun updateProfile(profile: Profile)
}
