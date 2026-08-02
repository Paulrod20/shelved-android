package com.paulrod.shelved.ui.auth

import com.paulrod.shelved.data.auth.AuthFailure

enum class AuthMessage {
    INVALID_EMAIL,
    SHORT_PASSWORD,
    PASSWORD_MISMATCH,
    MISSING_PASSWORD,
    RESET_EMAIL_REQUIRED,
    RESET_EMAIL_SENT,
    VERIFICATION_EMAIL_SENT,
    VERIFICATION_SEND_FAILED,
    GOOGLE_SIGN_IN_FAILED,
    WEAK_PASSWORD,
    ACCOUNT_EXISTS,
    INVALID_CREDENTIALS,
    NETWORK,
    UNKNOWN,
}

internal fun validateCreateAccount(email: String, password: String, confirmation: String): AuthMessage? = when {
    !email.isValidEmail() -> AuthMessage.INVALID_EMAIL
    password.length < 8 -> AuthMessage.SHORT_PASSWORD
    password != confirmation -> AuthMessage.PASSWORD_MISMATCH
    else -> null
}

internal fun validateSignIn(email: String, password: String): AuthMessage? = when {
    !email.isValidEmail() -> AuthMessage.INVALID_EMAIL
    password.isBlank() -> AuthMessage.MISSING_PASSWORD
    else -> null
}

internal fun validateResetEmail(email: String): AuthMessage? =
    if (email.isValidEmail()) null else AuthMessage.RESET_EMAIL_REQUIRED

internal fun AuthFailure.toAuthMessage(): AuthMessage = when (this) {
    AuthFailure.WEAK_PASSWORD -> AuthMessage.WEAK_PASSWORD
    AuthFailure.ACCOUNT_EXISTS -> AuthMessage.ACCOUNT_EXISTS
    AuthFailure.INVALID_CREDENTIALS -> AuthMessage.INVALID_CREDENTIALS
    AuthFailure.NETWORK -> AuthMessage.NETWORK
    AuthFailure.UNKNOWN -> AuthMessage.UNKNOWN
}

private fun String.isValidEmail(): Boolean =
    matches(Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
