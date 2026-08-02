package com.paulrod.shelved.ui.onboarding

import androidx.credentials.exceptions.GetCredentialCancellationException
import com.paulrod.shelved.data.OnboardingCompletionStore
import com.paulrod.shelved.data.auth.AuthGateway
import com.paulrod.shelved.data.auth.EmailAuthResult
import com.paulrod.shelved.data.auth.VerificationDelivery
import com.paulrod.shelved.ui.auth.AuthMessage
import com.paulrod.shelved.test.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class OnboardingViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var completionStore: FakeCompletionStore
    private lateinit var authGateway: FakeAuthGateway
    private lateinit var viewModel: OnboardingViewModel

    @Before
    fun setUp() {
        completionStore = FakeCompletionStore()
        authGateway = FakeAuthGateway()
        viewModel = OnboardingViewModel(completionStore, authGateway)
    }

    @Test
    fun completedPreferenceSkipsOnboarding() {
        val completedViewModel = OnboardingViewModel(FakeCompletionStore(isCompleted = true), authGateway)

        assertTrue(completedViewModel.uiState.value.isCompleted)
    }

    @Test
    fun localModeSignsOutAndPersistsCompletion() {
        viewModel.continueWithoutAccount()

        assertTrue(authGateway.didSignOut)
        assertTrue(completionStore.isCompleted)
        assertTrue(viewModel.uiState.value.isCompleted)
    }

    @Test
    fun verifiedEmailSignInCompletesOnboarding() = runTest {
        authGateway.signInResult = EmailAuthResult.SignedIn

        viewModel.signInWithEmail(" player@example.com ", "password")
        advanceUntilIdle()

        assertEquals("player@example.com", authGateway.lastEmail)
        assertTrue(completionStore.isCompleted)
        assertTrue(viewModel.uiState.value.isCompleted)
    }

    @Test
    fun failedVerificationDeliveryKeepsAccountAndOffersRetry() = runTest {
        authGateway.createResult = EmailAuthResult.VerificationRequired(
            email = "player@example.com",
            delivery = VerificationDelivery.SEND_FAILED,
        )

        viewModel.createEmailAccount("player@example.com", "password", "password")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isCompleted)
        assertEquals("player@example.com", state.verificationEmail)
        assertEquals(VerificationDelivery.SEND_FAILED, state.verificationDelivery)
        assertEquals(AuthMessage.VERIFICATION_SEND_FAILED, state.errorMessage)
    }

    @Test
    fun resendUpdatesVerificationDelivery() = runTest {
        viewModel.resendVerification()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(VerificationDelivery.SENT, state.verificationDelivery)
        assertEquals(AuthMessage.VERIFICATION_EMAIL_SENT, state.noticeMessage)
        assertFalse(state.isLoading)
    }

    @Test
    fun googleCancellationReturnsToIdleWithoutAnError() = runTest {
        viewModel.signInWithGoogle { throw GetCredentialCancellationException() }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isCompleted)
        assertNull(state.errorMessage)
    }

    @Test
    fun googleSignInCompletesAndPassesTokenToAuth() = runTest {
        viewModel.signInWithGoogle { "google-token" }
        advanceUntilIdle()

        assertEquals("google-token", authGateway.googleToken)
        assertTrue(completionStore.isCompleted)
    }
}

private class FakeCompletionStore(
    override var isCompleted: Boolean = false,
) : OnboardingCompletionStore {
    override fun complete() {
        isCompleted = true
    }
}

private class FakeAuthGateway : AuthGateway {
    var createResult: EmailAuthResult = EmailAuthResult.SignedIn
    var signInResult: EmailAuthResult = EmailAuthResult.SignedIn
    var didSignOut = false
    var lastEmail: String? = null
    var googleToken: String? = null

    override suspend fun createEmailAccount(email: String, password: String): EmailAuthResult {
        lastEmail = email
        return createResult
    }

    override suspend fun signInWithEmail(email: String, password: String): EmailAuthResult {
        lastEmail = email
        return signInResult
    }

    override suspend fun signInWithGoogle(idToken: String) {
        googleToken = idToken
    }

    override suspend fun sendPasswordReset(email: String) = Unit

    override suspend fun resendVerification() = Unit

    override fun signOut() {
        didSignOut = true
    }
}
