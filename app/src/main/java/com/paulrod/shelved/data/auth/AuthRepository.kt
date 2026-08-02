package com.paulrod.shelved.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
) : AuthGateway, AuthSessionProvider {
    override val currentSession: AuthSession
        get() = auth.currentUser.toAuthSession()

    override val sessions: Flow<AuthSession>
        get() = callbackFlow {
            val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                trySend(firebaseAuth.currentUser.toAuthSession())
            }
            auth.addAuthStateListener(listener)
            awaitClose { auth.removeAuthStateListener(listener) }
        }.distinctUntilChanged()

    override suspend fun createEmailAccount(email: String, password: String): EmailAuthResult {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val user = result.user ?: error("Firebase did not return a user.")
        val delivery = runCatching { user.sendEmailVerification().await() }
            .fold(
                onSuccess = { VerificationDelivery.SENT },
                onFailure = { VerificationDelivery.SEND_FAILED },
            )
        return EmailAuthResult.VerificationRequired(user.email ?: email, delivery)
    }

    override suspend fun signInWithEmail(email: String, password: String): EmailAuthResult {
        val user = auth.signInWithEmailAndPassword(email, password).await().user
            ?: error("Firebase did not return a user.")
        return if (user.isEmailVerified) {
            EmailAuthResult.SignedIn
        } else {
            EmailAuthResult.VerificationRequired(user.email ?: email, VerificationDelivery.REQUIRED)
        }
    }

    override suspend fun signInWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).await()
    }

    override suspend fun sendPasswordReset(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }

    override suspend fun resendVerification() {
        val user = auth.currentUser ?: error("No signed-in user.")
        user.sendEmailVerification().await()
    }

    override fun signOut() = auth.signOut()
}

private fun com.google.firebase.auth.FirebaseUser?.toAuthSession(): AuthSession =
    if (this == null) {
        AuthSession()
    } else {
        AuthSession(
            userId = uid,
            email = email,
            displayName = displayName,
            isEmailVerified = isEmailVerified,
        )
    }
