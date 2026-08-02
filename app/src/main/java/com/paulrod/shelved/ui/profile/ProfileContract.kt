package com.paulrod.shelved.ui.profile

import com.paulrod.shelved.data.model.Game
import com.paulrod.shelved.data.model.Profile
import com.paulrod.shelved.data.image.LocalImageSource

data class ProfileUiState(
    val profile: Profile = Profile(),
    val games: List<Game> = emptyList(),
    val favoriteGames: List<Game> = emptyList(),
    val isEditSheetVisible: Boolean = false,
    val isMenuSheetVisible: Boolean = false,
    val isProfileImageLoading: Boolean = false,
    val hasProfileImageError: Boolean = false,
    val editingProfileImagePath: String? = null,
)

sealed interface ProfileAction {
    data object EditRequested : ProfileAction
    data object EditDismissed : ProfileAction
    data object MenuRequested : ProfileAction
    data object MenuDismissed : ProfileAction
    data class ProfileImageSelected(val source: LocalImageSource) : ProfileAction
    data object ProfileImageRemoved : ProfileAction
    data class ProfileSaved(val profile: Profile) : ProfileAction
}
