# Shelved for Android

A native, Android-exclusive game library built with Kotlin and Jetpack Compose. The UI carries the original Shelved visual language into an edge-to-edge experience inspired by Samsung One UI, including a floating gesture-aware navigation bar.

## Private cloud backup

Signed-in libraries are backed up to Cloud Firestore under `users/{uid}` and remain private to that Firebase Authentication user. Try mode uses an in-memory library that is discarded when the app process closes and never contacts Firestore. Signing in during the trial imports its current games and profile into the authenticated library before cloud synchronization.

Before using cloud backup in a Firebase project:

1. Create a Cloud Firestore database in the Firebase console.
2. Deploy [`firestore.rules`](firestore.rules) with the Firebase CLI: `firebase deploy --only firestore:rules`.
3. Test sign-in, restore, deletion, and account switching against a non-production Firebase project before release.

Custom profile and cover images remain device-local for now because their filesystem paths are not portable to another Android device.

## Game search (IGDB)

Game search goes through the Cloudflare Worker in [`worker/`](worker) instead of exposing provider credentials in the Android app. The Worker obtains an app access token from Twitch and queries IGDB. Firebase App Check protects the custom API; signing in is not required, so Try mode can search too. The Worker runs on Cloudflare's free plan and Firebase can remain on Spark.

Before deploying game search:

1. In the [Twitch developer console](https://dev.twitch.tv/console/apps), register a confidential app. `http://localhost` is sufficient for the required OAuth redirect URL because Shelved uses the server-to-server client credentials flow.
2. Create a Cloudflare Worker named `shelved-game-api`.
3. In the Worker's **Settings > Variables and Secrets**, store the Twitch values as encrypted secrets named `TWITCH_CLIENT_ID` and `TWITCH_CLIENT_SECRET`. Never put their values in this repository.
4. Install, test, and deploy the Worker:

   ```shell
   cd worker
   pnpm install
   pnpm test
   pnpm typecheck
   pnpm deploy
   ```

5. In Firebase, open **Security > App Check**, select the Android app, and register the production app with Play Integrity and its SHA-256 signing fingerprint. For local debug builds, launch the app once, copy the App Check debug token from Logcat, then add it under the app's **Manage debug tokens** menu.

New catalog IDs use the `igdb:<id>` namespace. Existing games saved with legacy RAWG numeric IDs remain usable, but cannot request fresh provider details because IDs from the two catalogs are not interchangeable. `RAWG_API_KEY` is no longer read and can be removed from `local.properties`.
