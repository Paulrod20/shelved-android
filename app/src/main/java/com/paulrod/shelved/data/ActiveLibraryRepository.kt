package com.paulrod.shelved.data

import com.paulrod.shelved.data.model.Game
import com.paulrod.shelved.data.model.Profile
import com.paulrod.shelved.data.profile.ProfileRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

/** Routes UI operations to whichever library belongs to the current session. */
@OptIn(ExperimentalCoroutinesApi::class)
class ActiveLibraryRepository(
    initialRepository: LibraryRepository,
    scope: CoroutineScope,
) : ShelvedDataRepository, ProfileRepository {
    private val activeRepository = MutableStateFlow(initialRepository)

    override val games: StateFlow<List<Game>> = activeRepository
        .flatMapLatest { it.games }
        .stateIn(scope, SharingStarted.Eagerly, initialRepository.games.value)

    override val profile: StateFlow<Profile> = activeRepository
        .flatMapLatest { it.profile }
        .stateIn(scope, SharingStarted.Eagerly, initialRepository.profile.value)

    fun use(repository: LibraryRepository) {
        activeRepository.value = repository
    }

    override fun addGame(game: Game) = activeRepository.value.addGame(game)
    override fun updateGame(game: Game) = activeRepository.value.updateGame(game)
    override fun deleteGames(gameIds: Set<String>) = activeRepository.value.deleteGames(gameIds)
    override fun updateProfile(profile: Profile) = activeRepository.value.updateProfile(profile)
}
