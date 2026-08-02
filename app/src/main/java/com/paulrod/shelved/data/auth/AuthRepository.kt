package com.paulrod.shelved.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.FirebaseNetworkException
import kotlinx.coroutines.tasks.await

enum class VerificationDelivery { SENT, REQUIRED, SEND_FAILED }

enum class AuthFailure { WEAK_PASSWORD, ACCOUNT_EXISTS, INVALID_CREDENTIALS, NETWORK, UNKNOWN }

sealed interface EmailAuthResult {
    data object SignedIn : EmailAuthResult

    data class VerificationRequired(
        val email: String,
        val delivery: VerificationDelivery,
    ) : EmailAuthResult
}

interface AuthGateway {
    suspend fun createEmailAccount(email: String, password: String): EmailAuthResult
    suspend fun signInWithEmail(email: String, password: String): EmailAuthResult
    suspend fun signInWithGoogle(idToken: String)
    suspend fun sendPasswordReset(email: String)
    suspend fun resendVerification()
    fun signOut()
}

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
) : AuthGateway {
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

internal fun Throwable.authFailure(): AuthFailure = when (this) {
    is FirebaseAuthWeakPasswordException -> AuthFailure.WEAK_PASSWORD
    is FirebaseAuthUserCollisionException -> AuthFailure.ACCOUNT_EXISTS
    is FirebaseAuthInvalidCredentialsException -> AuthFailure.INVALID_CREDENTIALS
    is FirebaseNetworkException -> AuthFailure.NETWORK
    else -> AuthFailure.UNKNOWN
}
