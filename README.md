# Shelved for Android

A native, Android-exclusive game library built with Kotlin and Jetpack Compose. The UI carries the original Shelved visual language into an edge-to-edge experience inspired by Samsung One UI, including a floating gesture-aware navigation bar.

## Private cloud backup

Signed-in libraries are backed up to Cloud Firestore under `users/{uid}` and remain private to that Firebase Authentication user. Local mode stays fully persistent and does not contact Firestore.

Before using cloud backup in a Firebase project:

1. Create a Cloud Firestore database in the Firebase console.
2. Deploy [`firestore.rules`](firestore.rules) with the Firebase CLI: `firebase deploy --only firestore:rules`.
3. Test sign-in, restore, deletion, and account switching against a non-production Firebase project before release.

Custom profile and cover images remain device-local for now because their filesystem paths are not portable to another Android device.
