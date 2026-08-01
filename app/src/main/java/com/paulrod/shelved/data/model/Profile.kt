package com.paulrod.shelved.data.model

data class Profile(
    val displayName: String = "",
    val bio: String = "",
    val favoritePlatforms: List<Platform> = emptyList(),
    val favoriteGameIds: List<String> = emptyList(),
)

enum class Platform(val label: String) {
    PLAYSTATION("PlayStation"),
    XBOX("Xbox"),
    NINTENDO("Nintendo"),
    PC("PC"),
    HANDHELD("PC Handheld"),
}
