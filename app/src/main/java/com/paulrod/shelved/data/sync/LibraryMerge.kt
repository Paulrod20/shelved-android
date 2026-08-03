package com.paulrod.shelved.data.sync

import com.paulrod.shelved.data.model.Profile

/** First-sign-in policy: retain every game and prefer intentional data already on this device. */
internal object LibraryMerge {
    fun merge(local: LibrarySnapshot, cloud: LibrarySnapshot?): LibrarySnapshot {
        if (cloud == null) return local

        val localIds = local.games.mapTo(mutableSetOf()) { it.id }
        val games = local.games + cloud.games.filterNot { it.id in localIds }
        val gameIds = games.mapTo(mutableSetOf()) { it.id }

        return LibrarySnapshot(
            games = games,
            profile = mergeProfile(local.profile, cloud.profile, gameIds),
        )
    }

    private fun mergeProfile(local: Profile, cloud: Profile, validGameIds: Set<String>) = Profile(
        displayName = local.displayName.ifBlank { cloud.displayName },
        bio = local.bio.ifBlank { cloud.bio },
        profileImagePath = local.profileImagePath,
        favoritePlatforms = local.favoritePlatforms.ifEmpty { cloud.favoritePlatforms },
        favoriteGameIds = (local.favoriteGameIds + cloud.favoriteGameIds)
            .distinct()
            .filter { it in validGameIds },
    )
}
