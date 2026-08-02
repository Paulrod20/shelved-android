package com.paulrod.shelved.ui.onboarding

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OnboardingValidationTest {
    @Test
    fun createAccountRequiresValidMatchingCredentials() {
        assertEquals(OnboardingMessage.INVALID_EMAIL, validateCreateAccount("bad", "password", "password"))
        assertEquals(OnboardingMessage.SHORT_PASSWORD, validateCreateAccount("me@example.com", "short", "short"))
        assertEquals(OnboardingMessage.PASSWORD_MISMATCH, validateCreateAccount("me@example.com", "password", "different"))
        assertNull(validateCreateAccount("me@example.com", "password", "password"))
    }

    @Test
    fun signInAllowsExistingPasswordsButRejectsMissingValues() {
        assertEquals(OnboardingMessage.INVALID_EMAIL, validateSignIn("", "password"))
        assertEquals(OnboardingMessage.MISSING_PASSWORD, validateSignIn("me@example.com", ""))
        assertNull(validateSignIn("me@example.com", "legacy"))
    }

    @Test
    fun resetRequiresAnEmailAddress() {
        assertEquals(OnboardingMessage.RESET_EMAIL_REQUIRED, validateResetEmail("not-an-email"))
        assertNull(validateResetEmail("me@example.com"))
    }
}
