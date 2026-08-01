package com.paulrod.shelved.ui.profile

import com.paulrod.shelved.data.model.Game
import com.paulrod.shelved.data.model.Profile

data class ProfileUiState(
    val profile: Profile = Profile(),
    val games: List<Game> = emptyList(),
    val favoriteGames: List<Game> = emptyList(),
    val isEditSheetVisible: Boolean = false,
    val isMenuSheetVisible: Boolean = false,
)

sealed interface ProfileAction {
    data object EditRequested : ProfileAction
    data object EditDismissed : ProfileAction
    data object MenuRequested : ProfileAction
    data object MenuDismissed : ProfileAction
    data class ProfileSaved(val profile: Profile) : ProfileAction
}
