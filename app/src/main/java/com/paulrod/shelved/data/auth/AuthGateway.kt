package com.paulrod.shelved.data.auth

import kotlinx.coroutines.flow.Flow

interface AuthGateway {
    suspend fun createEmailAccount(email: String, password: String): EmailAuthResult
    suspend fun signInWithEmail(email: String, password: String): EmailAuthResult
    suspend fun signInWithGoogle(idToken: String)
    suspend fun sendPasswordReset(email: String)
    suspend fun resendVerification()
    fun signOut()
}

interface AuthSessionProvider {
    val currentSession: AuthSession
    val sessions: Flow<AuthSession>
}
