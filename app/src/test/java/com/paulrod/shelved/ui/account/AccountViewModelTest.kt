package com.paulrod.shelved.ui.account

import androidx.credentials.exceptions.GetCredentialCancellationException
import com.paulrod.shelved.data.auth.AuthGateway
import com.paulrod.shelved.data.auth.AuthSession
import com.paulrod.shelved.data.auth.AuthSessionProvider
import com.paulrod.shelved.data.auth.EmailAuthResult
import com.paulrod.shelved.data.auth.VerificationDelivery
import com.paulrod.shelved.test.MainDispatcherRule
import com.paulrod.shelved.ui.auth.AuthMessage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AccountViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var auth: FakeAccountAuth
    private lateinit var viewModel: AccountViewModel

    @Before
    fun setUp() {
        auth = FakeAccountAuth()
        viewModel = AccountViewModel(auth, auth)
    }

    @Test
    fun existingSessionIsShownImmediately() {
        val signedIn = AuthSession(userId = "user", email = "player@example.com", isEmailVerified = true)
        auth = FakeAccountAuth(signedIn)

        viewModel = AccountViewModel(auth, auth)

        assertEquals(signedIn, viewModel.uiState.value.session)
    }

    @Test
    fun emailSignInTrimsEmailAndClosesScreen() = runTest {
        viewModel.showSignIn()

        viewModel.signInWithEmail(" player@example.com ", "password")
        advanceUntilIdle()

        assertEquals("player@example.com", auth.lastEmail)
        assertFalse(viewModel.uiState.value.isSignInVisible)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun createAccountTrimsEmailAndShowsVerificationBeforeClosing() = runTest {
        auth.createResult = EmailAuthResult.VerificationRequired(
            "player@example.com",
            VerificationDelivery.SENT,
        )
        viewModel.showSignIn()

        viewModel.createEmailAccount(" player@example.com ", "password", "password")
        advanceUntilIdle()

        assertEquals("player@example.com", auth.lastCreatedEmail)
        assertEquals("player@example.com", viewModel.uiState.value.verificationEmail)
        assertTrue(viewModel.uiState.value.isSignInVisible)

        viewModel.continueAfterVerification()

        assertFalse(viewModel.uiState.value.isSignInVisible)
        assertNull(viewModel.uiState.value.verificationEmail)
    }

    @Test
    fun mismatchedCreateAccountPasswordsStayOnScreen() {
        viewModel.showSignIn()

        viewModel.createEmailAccount("player@example.com", "password", "different")

        assertNull(auth.lastCreatedEmail)
        assertEquals(AuthMessage.PASSWORD_MISMATCH, viewModel.uiState.value.errorMessage)
    }

    @Test
    fun invalidCredentialsStayOnScreen() {
        viewModel.showSignIn()

        viewModel.signInWithEmail("bad-email", "")

        assertTrue(viewModel.uiState.value.isSignInVisible)
        assertEquals(AuthMessage.INVALID_EMAIL, viewModel.uiState.value.errorMessage)
    }

    @Test
    fun googleCancellationReturnsToIdle() = runTest {
        viewModel.showSignIn()

        viewModel.signInWithGoogle { throw GetCredentialCancellationException() }
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSignInVisible)
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun signOutClearsSessionWithoutWaitingForListener() {
        auth.emit(AuthSession(userId = "user", email = "player@example.com"))
        viewModel.signOut()

        assertTrue(auth.didSignOut)
        assertFalse(viewModel.uiState.value.session.isSignedIn)
    }
}

private class FakeAccountAuth(
    initialSession: AuthSession = AuthSession(),
) : AuthGateway, AuthSessionProvider {
    private val sessionFlow = MutableStateFlow(initialSession)

    override val currentSession: AuthSession get() = sessionFlow.value
    override val sessions: Flow<AuthSession> = sessionFlow

    var lastEmail: String? = null
    var lastCreatedEmail: String? = null
    var createResult: EmailAuthResult = EmailAuthResult.SignedIn
    var didSignOut = false

    fun emit(session: AuthSession) {
        sessionFlow.value = session
    }

    override suspend fun createEmailAccount(email: String, password: String): EmailAuthResult {
        lastCreatedEmail = email
        return createResult
    }

    override suspend fun signInWithEmail(email: String, password: String): EmailAuthResult {
        lastEmail = email
        return EmailAuthResult.SignedIn
    }

    override suspend fun signInWithGoogle(idToken: String) = Unit

    override suspend fun sendPasswordReset(email: String) = Unit

    override suspend fun resendVerification() = Unit

    override fun signOut() {
        didSignOut = true
        emit(AuthSession())
    }
}
