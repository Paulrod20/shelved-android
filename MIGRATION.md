# Shelved native Android migration

This repository is a native Android application: Kotlin and Jetpack Compose only. It has no React Native, Expo, JavaScript runtime, or cross-platform UI layer.

| Existing app | Native Android implementation |
| --- | --- |
| React Native UI | Jetpack Compose composables |
| React Navigation tabs | AndroidX navigation state |
| AsyncStorage | Device-local repository with a versionable JSON persistence shape |
| React contexts/hooks | `ShelvedViewModel`, repository, and `StateFlow` |
| `fetch` / `AbortController` | Cloudflare Worker + Kotlin coroutines + LRU search cache |
| React Native sheets | Material 3 `ModalBottomSheet` |

The Android package tree mirrors the RN feature boundaries:

```text
app/src/main/java/com/paulrod/shelved/
  data/          # Persistent repository, Cloudflare-backed IGDB catalog, and models
  ui/            # ViewModel and StateFlows
  ui/navigation/ # Backlog, Search, Stats, Profile destinations
  ui/theme/      # Shelved colors and theme
```

The first native feature pass is complete: backlog management, search, profile, stats, bottom sheets, edge-to-edge behavior, and floating navigation are implemented. Future schema growth can move the repository to Room without changing the UI-facing StateFlow contract.
