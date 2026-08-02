package com.paulrod.shelved.ui.onboarding

import com.paulrod.shelved.data.auth.VerificationDelivery

enum class EmailAuthMode { CREATE_ACCOUNT, SIGN_IN }

enum class OnboardingMessage {
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
    AUTH_UNKNOWN,
}

data class OnboardingUiState(
    val isCompleted: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: OnboardingMessage? = null,
    val noticeMessage: OnboardingMessage? = null,
    val verificationEmail: String? = null,
    val verificationDelivery: VerificationDelivery? = null,
)

internal fun validateCreateAccount(email: String, password: String, confirmation: String): OnboardingMessage? = when {
    !email.isValidEmail() -> OnboardingMessage.INVALID_EMAIL
    password.length < 8 -> OnboardingMessage.SHORT_PASSWORD
    password != confirmation -> OnboardingMessage.PASSWORD_MISMATCH
    else -> null
}

internal fun validateSignIn(email: String, password: String): OnboardingMessage? = when {
    !email.isValidEmail() -> OnboardingMessage.INVALID_EMAIL
    password.isBlank() -> OnboardingMessage.MISSING_PASSWORD
    else -> null
}

internal fun validateResetEmail(email: String): OnboardingMessage? =
    if (email.isValidEmail()) null else OnboardingMessage.RESET_EMAIL_REQUIRED

private fun String.isValidEmail(): Boolean =
    matches(Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
