package com.paulrod.shelved.ui.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthValidationTest {
    @Test
    fun createAccountRequiresValidMatchingCredentials() {
        assertEquals(AuthMessage.INVALID_EMAIL, validateCreateAccount("bad", "password", "password"))
        assertEquals(AuthMessage.SHORT_PASSWORD, validateCreateAccount("me@example.com", "short", "short"))
        assertEquals(AuthMessage.PASSWORD_MISMATCH, validateCreateAccount("me@example.com", "password", "different"))
        assertNull(validateCreateAccount("me@example.com", "password", "password"))
    }

    @Test
    fun signInAllowsExistingPasswordsButRejectsMissingValues() {
        assertEquals(AuthMessage.INVALID_EMAIL, validateSignIn("", "password"))
        assertEquals(AuthMessage.MISSING_PASSWORD, validateSignIn("me@example.com", ""))
        assertNull(validateSignIn("me@example.com", "legacy"))
    }

    @Test
    fun resetRequiresAnEmailAddress() {
        assertEquals(AuthMessage.RESET_EMAIL_REQUIRED, validateResetEmail("not-an-email"))
        assertNull(validateResetEmail("me@example.com"))
    }
}
