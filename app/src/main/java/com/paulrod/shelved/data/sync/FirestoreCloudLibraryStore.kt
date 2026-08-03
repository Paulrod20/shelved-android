package com.paulrod.shelved.data.sync

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.WriteBatch
import com.paulrod.shelved.data.model.Game
import com.paulrod.shelved.data.model.GameNote
import com.paulrod.shelved.data.model.GameStatus
import com.paulrod.shelved.data.model.Platform
import com.paulrod.shelved.data.model.Profile
import kotlinx.coroutines.tasks.await

class FirestoreCloudLibraryStore(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : CloudLibraryStore {
    override suspend fun load(userId: String): LibrarySnapshot? {
        val user = userDocument(userId).get().await()
        val gameDocuments = user.reference.collection(GAMES_COLLECTION).get().await().documents
        if (!user.exists() && gameDocuments.isEmpty()) return null

        val games = gameDocuments.mapNotNull { document ->
                document.data?.toGame(document.id)
            }
        val gameOrder = user.get(GAME_ORDER_FIELD) as? List<*>
        val orderById = gameOrder.orEmpty().mapIndexedNotNull { index, id ->
            (id as? String)?.let { it to index }
        }.toMap()

        return LibrarySnapshot(
            games = games.sortedBy { orderById[it.id] ?: Int.MAX_VALUE },
            profile = user.get(PROFILE_FIELD).asStringMap()?.toProfile() ?: Profile(),
        )
    }

    override suspend fun apply(userId: String, changes: LibraryChanges) {
        if (changes.isEmpty) return

        val user = userDocument(userId)
        val operations = buildList<(WriteBatch) -> Unit> {
            if (changes.profile != null || changes.gameOrder != null) {
                add { batch ->
                    val userUpdates = buildMap<String, Any> {
                        put(SCHEMA_VERSION_FIELD, SCHEMA_VERSION)
                        put(UPDATED_AT_FIELD, FieldValue.serverTimestamp())
                        changes.profile?.let { put(PROFILE_FIELD, it.toCloudMap()) }
                        changes.gameOrder?.let { put(GAME_ORDER_FIELD, it) }
                    }
                    batch.set(
                        user,
                        userUpdates,
                        SetOptions.merge(),
                    )
                }
            }
            changes.gamesToUpsert.forEach { game ->
                add { batch ->
                    batch.set(
                        user.collection(GAMES_COLLECTION).document(game.id),
                        game.toCloudMap() + (UPDATED_AT_FIELD to FieldValue.serverTimestamp()),
                    )
                }
            }
            changes.gameIdsToDelete.forEach { gameId ->
                add { batch -> batch.delete(user.collection(GAMES_COLLECTION).document(gameId)) }
            }
        }

        operations.chunked(MAX_BATCH_OPERATIONS).forEach { operationsInBatch ->
            firestore.runBatch { batch -> operationsInBatch.forEach { it(batch) } }.await()
        }
    }

    private fun userDocument(userId: String) =
        firestore.collection(USERS_COLLECTION).document(userId)

    private fun Profile.toCloudMap(): Map<String, Any> = mapOf(
        "displayName" to displayName,
        "bio" to bio,
        "favoritePlatforms" to favoritePlatforms.map(Platform::name),
        "favoriteGameIds" to favoriteGameIds,
        "visibility" to PRIVATE_VISIBILITY,
    )

    private fun Game.toCloudMap(): Map<String, Any?> = mapOf(
        "name" to name,
        "coverImageUrl" to coverImageUrl,
        "status" to status.name,
        "hoursPlayed" to hoursPlayed,
        "notes" to notes.map { note ->
            mapOf("text" to note.text, "createdAtEpochMillis" to note.createdAtEpochMillis)
        },
        "released" to released,
        "playtime" to playtime,
        "platforms" to platforms,
        "description" to description,
    )

    private fun Map<String, Any?>.toProfile() = Profile(
        displayName = string("displayName").orEmpty(),
        bio = string("bio").orEmpty(),
        favoritePlatforms = stringList("favoritePlatforms")
            .mapNotNull { stored -> runCatching { Platform.valueOf(stored) }.getOrNull() }
            .distinct(),
        favoriteGameIds = stringList("favoriteGameIds").distinct(),
    )

    private fun Map<String, Any?>.toGame(id: String): Game? {
        val name = string("name") ?: return null
        return Game(
            id = id,
            name = name,
            coverImageUrl = string("coverImageUrl"),
            status = string("status")?.let { stored ->
                runCatching { GameStatus.valueOf(stored) }.getOrNull()
            } ?: GameStatus.BACKLOG,
            hoursPlayed = number("hoursPlayed")?.toInt(),
            notes = mapList("notes").mapNotNull { note ->
                note.string("text")?.let { text ->
                    GameNote(text, note.number("createdAtEpochMillis")?.toLong() ?: 0L)
                }
            },
            released = string("released"),
            playtime = number("playtime")?.toInt(),
            platforms = stringList("platforms"),
            description = string("description"),
        )
    }

    private fun Any?.asStringMap(): Map<String, Any?>? =
        (this as? Map<*, *>)?.entries?.associate { (key, value) -> key.toString() to value }

    private fun Map<String, Any?>.string(key: String) = this[key] as? String
    private fun Map<String, Any?>.number(key: String) = this[key] as? Number
    private fun Map<String, Any?>.stringList(key: String) =
        (this[key] as? List<*>)?.mapNotNull { it as? String }.orEmpty()
    private fun Map<String, Any?>.mapList(key: String) =
        (this[key] as? List<*>)?.mapNotNull { it.asStringMap() }.orEmpty()

    private companion object {
        const val USERS_COLLECTION = "users"
        const val GAMES_COLLECTION = "games"
        const val PROFILE_FIELD = "profile"
        const val GAME_ORDER_FIELD = "gameOrder"
        const val SCHEMA_VERSION_FIELD = "schemaVersion"
        const val UPDATED_AT_FIELD = "updatedAt"
        const val PRIVATE_VISIBILITY = "private"
        const val SCHEMA_VERSION = 1L
        const val MAX_BATCH_OPERATIONS = 450
    }
}
