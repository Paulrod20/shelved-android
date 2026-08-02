package com.paulrod.shelved.data.auth

enum class VerificationDelivery { SENT, REQUIRED, SEND_FAILED }

enum class AuthFailure { WEAK_PASSWORD, ACCOUNT_EXISTS, INVALID_CREDENTIALS, NETWORK, UNKNOWN }

data class AuthSession(
    val userId: String? = null,
    val email: String? = null,
    val displayName: String? = null,
    val isEmailVerified: Boolean = false,
) {
    val isSignedIn: Boolean get() = userId != null
}

sealed interface EmailAuthResult {
    data object SignedIn : EmailAuthResult

    data class VerificationRequired(
        val email: String,
        val delivery: VerificationDelivery,
    ) : EmailAuthResult
}
