package com.paulrod.shelved.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulrod.shelved.data.image.LocalImageSource
import com.paulrod.shelved.data.profile.ProfileImageStorage
import com.paulrod.shelved.data.profile.ProfileRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repository: ProfileRepository,
    private val imageStorage: ProfileImageStorage,
) : ViewModel() {
    private val controls = MutableStateFlow(ProfileControls())

    val uiState: StateFlow<ProfileUiState> = combine(
        repository.profile,
        repository.games,
        controls,
    ) { profile, games, controls ->
        ProfileUiState(
            profile = profile,
            games = games,
            favoriteGames = profile.favoriteGameIds.mapNotNull { id -> games.find { it.id == id } },
            isEditSheetVisible = controls.isEditSheetVisible,
            isMenuSheetVisible = controls.isMenuSheetVisible,
            isProfileImageLoading = controls.isProfileImageLoading,
            hasProfileImageError = controls.hasProfileImageError,
            editingProfileImagePath = controls.editingProfileImagePath,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())

    fun onAction(action: ProfileAction) {
        when (action) {
            ProfileAction.EditRequested -> controls.update {
                it.copy(
                    isEditSheetVisible = true,
                    hasProfileImageError = false,
                    editingProfileImagePath = repository.profile.value.profileImagePath,
                )
            }
            ProfileAction.EditDismissed -> dismissEditor()
            ProfileAction.MenuRequested -> controls.update { it.copy(isMenuSheetVisible = true) }
            ProfileAction.MenuDismissed -> controls.update { it.copy(isMenuSheetVisible = false) }
            is ProfileAction.ProfileImageSelected -> updateProfileImage(action.source)
            ProfileAction.ProfileImageRemoved -> removeProfileImage()
            is ProfileAction.ProfileSaved -> saveProfile(action)
        }
    }

    private fun updateProfileImage(source: LocalImageSource) {
        if (controls.value.isProfileImageLoading) return
        controls.update { it.copy(isProfileImageLoading = true, hasProfileImageError = false) }

        viewModelScope.launch {
            try {
                useSavedImage(imageStorage.save(source))
            } catch (error: Throwable) {
                if (error is CancellationException) throw error
                controls.update {
                    it.copy(isProfileImageLoading = false, hasProfileImageError = true)
                }
            }
        }
    }

    private suspend fun useSavedImage(path: String) {
        val currentControls = controls.value
        if (!currentControls.isEditSheetVisible) {
            imageStorage.remove(path)
            controls.update { it.copy(isProfileImageLoading = false) }
            return
        }

        val committedPath = repository.profile.value.profileImagePath
        val previousDraft = currentControls.editingProfileImagePath
        if (previousDraft != committedPath) imageStorage.remove(previousDraft)
        controls.update {
            it.copy(
                isProfileImageLoading = false,
                editingProfileImagePath = path,
            )
        }
    }

    private fun removeProfileImage() {
        if (controls.value.isProfileImageLoading) return
        val path = controls.value.editingProfileImagePath
        val committedPath = repository.profile.value.profileImagePath
        controls.update { it.copy(isProfileImageLoading = true, hasProfileImageError = false) }

        viewModelScope.launch {
            runCatching {
                if (path != committedPath) imageStorage.remove(path)
            }
                .onSuccess {
                    controls.update {
                        it.copy(isProfileImageLoading = false, editingProfileImagePath = null)
                    }
                }
                .onFailure {
                    controls.update {
                        it.copy(isProfileImageLoading = false, hasProfileImageError = true)
                    }
                }
        }
    }

    private fun saveProfile(action: ProfileAction.ProfileSaved) {
        val previousPath = repository.profile.value.profileImagePath
        repository.updateProfile(action.profile)
        controls.update {
            it.copy(
                isEditSheetVisible = false,
                hasProfileImageError = false,
                editingProfileImagePath = null,
            )
        }
        if (previousPath != action.profile.profileImagePath) {
            viewModelScope.launch { imageStorage.remove(previousPath) }
        }
    }

    private fun dismissEditor() {
        val draftPath = controls.value.editingProfileImagePath
        val committedPath = repository.profile.value.profileImagePath
        controls.update {
            it.copy(
                isEditSheetVisible = false,
                hasProfileImageError = false,
                editingProfileImagePath = null,
            )
        }
        if (draftPath != committedPath) {
            viewModelScope.launch { imageStorage.remove(draftPath) }
        }
    }
}

private data class ProfileControls(
    val isEditSheetVisible: Boolean = false,
    val isMenuSheetVisible: Boolean = false,
    val isProfileImageLoading: Boolean = false,
    val hasProfileImageError: Boolean = false,
    val editingProfileImagePath: String? = null,
)

private inline fun <T> MutableStateFlow<T>.update(transform: (T) -> T) {
    value = transform(value)
}
