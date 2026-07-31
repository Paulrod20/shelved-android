# Shelved for Android

A native, Android-exclusive game library built with Kotlin and Jetpack Compose. The UI carries the original Shelved visual language into an edge-to-edge experience inspired by Samsung One UI, including a floating gesture-aware navigation bar.

## Run it

1. Open this repository root in Android Studio.
2. Set the Gradle JDK to Android Studio's embedded JDK (21) if prompted.
3. Let Gradle sync, then select an emulator or device running API 26+.
4. Run the `app` configuration.

## Game search

Shelved uses RAWG for game search and cover art. Put your key in your user Gradle properties (never commit it):

```properties
# ~/.gradle/gradle.properties
RAWG_API_KEY=your-key-here
```

Then rebuild the app so the key is placed in the generated debug/release configuration.

## What is implemented

- Samsung-inspired floating navigation with Backlog, Search, Stats, and Profile
- Edge-to-edge layout with gesture-navigation and display-cutout insets
- Persistent game collection, status filters, hours, and notes
- Debounced RAWG search with an in-memory LRU cache
- Profile editing, favorite platform, and up to three favorite games
- Collection statistics and native Material bottom sheets

This is a native-only app. See [MIGRATION.md](MIGRATION.md) for the architecture and feature mapping.
