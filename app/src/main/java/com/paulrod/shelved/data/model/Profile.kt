package com.paulrod.shelved.data.model

data class Profile(
    val displayName: String = "",
    val bio: String = "",
    val favoritePlatform: Platform? = null,
    val favoriteGameIds: List<String> = emptyList(),
)

enum class Platform(val label: String) {
    PLAYSTATION("PlayStation"),
    XBOX("Xbox"),
    SWITCH("Nintendo Switch"),
    PC("PC"),
    STEAM_DECK("Steam Deck"),
    XBOX_ALLY_X("Xbox Ally X"),
    XBOX_ALLY("Xbox Ally"),
    ROG_ALLY("Rog Ally"),
    ROG_ALLY_X("Rog Ally X"),
    LEGION_GO("Legion Go"),
    LEGION_GO_S("Legion Go S"),
    LEGION_GO_2("Legion Go 2"),
}
