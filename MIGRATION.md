# Shelved native Android migration

This repository is a native Android application: Kotlin and Jetpack Compose only. It has no React Native, Expo, JavaScript runtime, or cross-platform UI layer.

| Existing app | Native Android implementation |
| --- | --- |
| React Native UI | Jetpack Compose composables |
| React Navigation tabs | AndroidX navigation state |
| AsyncStorage | Room for games and DataStore for profile/preferences |
| React contexts/hooks | ViewModels, repositories, and `StateFlow` |
| `fetch` / `AbortController` | Retrofit + OkHttp + Kotlin coroutines |
| React Native sheets | Material 3 `ModalBottomSheet` |

The Android package tree mirrors the RN feature boundaries:

```text
app/src/main/java/com/paulrod/shelved/
  data/          # Room, DataStore, RAWG API, repositories
  ui/components/ # GameCard, StatusTabs, bottom sheets, shared scaffolds
  ui/navigation/ # Backlog, Search, Stats, Profile destinations
  ui/screens/    # one Compose screen per existing RN screen
  ui/theme/      # Shelved colors and theme
```

The next migration pass will implement the Room-backed backlog and the Add Game flow, then the RAWG client, profile, and stats.
