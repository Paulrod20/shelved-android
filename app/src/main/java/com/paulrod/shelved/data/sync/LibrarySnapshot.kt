package com.paulrod.shelved.data.sync

import com.paulrod.shelved.data.model.Game
import com.paulrod.shelved.data.model.Profile

data class LibrarySnapshot(
    val games: List<Game> = emptyList(),
    val profile: Profile = Profile(),
) {
    /** Paths refer to files on one Android device and must never be restored elsewhere. */
    fun withoutDeviceOnlyData() = copy(
        games = games.map { it.copy(customCoverImagePath = null) },
        profile = profile.copy(profileImagePath = null),
    )
}

data class LibraryChanges(
    val profile: Profile? = null,
    val gameOrder: List<String>? = null,
    val gamesToUpsert: List<Game> = emptyList(),
    val gameIdsToDelete: Set<String> = emptySet(),
) {
    val isEmpty: Boolean
        get() = profile == null && gameOrder == null && gamesToUpsert.isEmpty() && gameIdsToDelete.isEmpty()
}

internal fun LibrarySnapshot.changesSince(previous: LibrarySnapshot?): LibraryChanges {
    val current = withoutDeviceOnlyData()
    val old = previous?.withoutDeviceOnlyData()
    val oldGames = old?.games.orEmpty().associateBy(Game::id)
    val currentGames = current.games.associateBy(Game::id)

    return LibraryChanges(
        profile = current.profile.takeIf { it != old?.profile },
        gameOrder = current.games.map(Game::id).takeIf { order ->
            order != old?.games.orEmpty().map(Game::id)
        },
        gamesToUpsert = current.games.filter { it != oldGames[it.id] },
        gameIdsToDelete = oldGames.keys - currentGames.keys,
    )
}
