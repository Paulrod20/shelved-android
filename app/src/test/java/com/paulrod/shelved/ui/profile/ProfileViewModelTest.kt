package com.paulrod.shelved.ui.profile

import com.paulrod.shelved.data.model.Game
import com.paulrod.shelved.data.model.Profile
import com.paulrod.shelved.data.profile.ProfileImageSource
import com.paulrod.shelved.data.profile.ProfileImageStorage
import com.paulrod.shelved.data.profile.ProfileRepository
import com.paulrod.shelved.test.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: FakeProfileRepository
    private lateinit var imageStorage: FakeProfileImageStorage
    private lateinit var viewModel: ProfileViewModel

    @Before
    fun setUp() {
        repository = FakeProfileRepository(
            initialProfile = Profile(displayName = "Player", profileImagePath = OLD_IMAGE),
        )
        imageStorage = FakeProfileImageStorage()
        viewModel = ProfileViewModel(repository, imageStorage)
    }

    @Test
    fun selectingImageCreatesDraftWithoutChangingSavedProfile() = runTest {
        observeState()
        viewModel.onAction(ProfileAction.EditRequested)
        viewModel.onAction(ProfileAction.ProfileImageSelected(ProfileImageSource("content://avatar")))
        advanceUntilIdle()

        assertEquals(OLD_IMAGE, repository.profile.value.profileImagePath)
        assertEquals(NEW_IMAGE, viewModel.uiState.value.editingProfileImagePath)
        assertTrue(viewModel.uiState.value.isEditSheetVisible)
        assertEquals(listOf(ProfileImageSource("content://avatar")), imageStorage.savedSources)
    }

    @Test
    fun saveCommitsDraftAndRemovesPreviousImage() = runTest {
        observeState()
        viewModel.onAction(ProfileAction.EditRequested)
        viewModel.onAction(ProfileAction.ProfileImageSelected(ProfileImageSource("content://avatar")))
        advanceUntilIdle()

        viewModel.onAction(
            ProfileAction.ProfileSaved(
                repository.profile.value.copy(profileImagePath = viewModel.uiState.value.editingProfileImagePath),
            ),
        )
        advanceUntilIdle()

        assertEquals(NEW_IMAGE, repository.profile.value.profileImagePath)
        assertEquals(listOf(OLD_IMAGE), imageStorage.removedPaths)
        assertFalse(viewModel.uiState.value.isEditSheetVisible)
        assertNull(viewModel.uiState.value.editingProfileImagePath)
    }

    @Test
    fun dismissDiscardsDraftAndKeepsSavedProfile() = runTest {
        observeState()
        viewModel.onAction(ProfileAction.EditRequested)
        viewModel.onAction(ProfileAction.ProfileImageSelected(ProfileImageSource("content://avatar")))
        advanceUntilIdle()

        viewModel.onAction(ProfileAction.EditDismissed)
        advanceUntilIdle()

        assertEquals(OLD_IMAGE, repository.profile.value.profileImagePath)
        assertEquals(listOf(NEW_IMAGE), imageStorage.removedPaths)
        assertFalse(viewModel.uiState.value.isEditSheetVisible)
    }

    @Test
    fun useDefaultOnlyDeletesSavedImageAfterSave() = runTest {
        observeState()
        viewModel.onAction(ProfileAction.EditRequested)
        viewModel.onAction(ProfileAction.ProfileImageRemoved)
        advanceUntilIdle()

        assertEquals(OLD_IMAGE, repository.profile.value.profileImagePath)
        assertNull(viewModel.uiState.value.editingProfileImagePath)
        assertTrue(imageStorage.removedPaths.isEmpty())

        viewModel.onAction(
            ProfileAction.ProfileSaved(repository.profile.value.copy(profileImagePath = null)),
        )
        advanceUntilIdle()

        assertNull(repository.profile.value.profileImagePath)
        assertEquals(listOf(OLD_IMAGE), imageStorage.removedPaths)
    }

    private fun TestScope.observeState() {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
    }

    private companion object {
        const val OLD_IMAGE = "/profile_images/old.jpg"
        const val NEW_IMAGE = "/profile_images/new.jpg"
    }
}

private class FakeProfileRepository(initialProfile: Profile) : ProfileRepository {
    private val profileState = MutableStateFlow(initialProfile)

    override val games: StateFlow<List<Game>> = MutableStateFlow(emptyList())
    override val profile: StateFlow<Profile> = profileState

    override fun updateProfile(profile: Profile) {
        profileState.value = profile
    }
}

private class FakeProfileImageStorage : ProfileImageStorage {
    val savedSources = mutableListOf<ProfileImageSource>()
    val removedPaths = mutableListOf<String>()

    override suspend fun save(source: ProfileImageSource): String {
        savedSources += source
        return "/profile_images/new.jpg"
    }

    override suspend fun remove(path: String?) {
        path?.let(removedPaths::add)
    }
}
