package com.paulrod.shelved.ui.onboarding

import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulrod.shelved.data.OnboardingCompletionStore
import com.paulrod.shelved.data.auth.AuthGateway
import com.paulrod.shelved.data.auth.EmailAuthResult
import com.paulrod.shelved.data.auth.VerificationDelivery
import com.paulrod.shelved.data.auth.authFailure
import com.paulrod.shelved.ui.auth.AuthMessage
import com.paulrod.shelved.ui.auth.toAuthMessage
import com.paulrod.shelved.ui.auth.validateCreateAccount
import com.paulrod.shelved.ui.auth.validateResetEmail
import com.paulrod.shelved.ui.auth.validateSignIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val preferences: OnboardingCompletionStore,
    private val authRepository: AuthGateway,
) : ViewModel() {
    private val _uiState = MutableStateFlow(OnboardingUiState(isCompleted = preferences.isCompleted))

    val uiState: StateFlow<OnboardingUiState> = _uiState

    fun continueWithoutAccount() {
        authRepository.signOut()
        completeOnboarding()
    }

    fun continueAfterVerification() = completeOnboarding()

    fun signInWithGoogle(idTokenProvider: suspend () -> String) {
        launchAuth(
            operation = {
                authRepository.signInWithGoogle(idTokenProvider())
            },
            onSuccess = { completeOnboarding() },
            onError = { error ->
                if (error is GetCredentialCancellationException) clearLoading()
                else showError(AuthMessage.GOOGLE_SIGN_IN_FAILED)
            },
        )
    }

    fun createEmailAccount(email: String, password: String, confirmation: String) {
        validateCreateAccount(email.trim(), password, confirmation)?.let {
            showError(it)
            return
        }
        launchAuth(
            operation = { authRepository.createEmailAccount(email.trim(), password) },
            onSuccess = ::handleEmailAuthResult,
        )
    }

    fun signInWithEmail(email: String, password: String) {
        validateSignIn(email.trim(), password)?.let {
            showError(it)
            return
        }
        launchAuth(
            operation = { authRepository.signInWithEmail(email.trim(), password) },
            onSuccess = ::handleEmailAuthResult,
        )
    }

    fun sendPasswordReset(email: String) {
        validateResetEmail(email.trim())?.let {
            showError(it)
            return
        }
        launchAuth(
            operation = { authRepository.sendPasswordReset(email.trim()) },
            onSuccess = {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = null,
                    noticeMessage = AuthMessage.RESET_EMAIL_SENT,
                )
            },
        )
    }

    fun resendVerification() {
        launchAuth(
            operation = authRepository::resendVerification,
            onSuccess = {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = null,
                    noticeMessage = AuthMessage.VERIFICATION_EMAIL_SENT,
                    verificationDelivery = VerificationDelivery.SENT,
                )
            },
        )
    }

    fun clearFeedback() {
        _uiState.value = _uiState.value.copy(errorMessage = null, noticeMessage = null)
    }

    private fun completeOnboarding() {
        preferences.complete()
        _uiState.value = OnboardingUiState(isCompleted = true)
    }

    private fun handleEmailAuthResult(result: EmailAuthResult) {
        when (result) {
            EmailAuthResult.SignedIn -> completeOnboarding()
            is EmailAuthResult.VerificationRequired -> showVerification(result)
        }
    }

    private fun showVerification(result: EmailAuthResult.VerificationRequired) {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            errorMessage = if (result.delivery == VerificationDelivery.SEND_FAILED) {
                AuthMessage.VERIFICATION_SEND_FAILED
            } else {
                null
            },
            noticeMessage = null,
            verificationEmail = result.email,
            verificationDelivery = result.delivery,
        )
    }

    private fun <T> launchAuth(
        operation: suspend () -> T,
        onSuccess: (T) -> Unit,
        onError: (Throwable) -> Unit = { showError(it.authFailure().toAuthMessage()) },
    ) {
        if (_uiState.value.isLoading) return
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, noticeMessage = null)
        viewModelScope.launch {
            try {
                onSuccess(operation())
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Throwable) {
                onError(error)
            }
        }
    }

    private fun showError(message: AuthMessage) {
        _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = message, noticeMessage = null)
    }

    private fun clearLoading() {
        _uiState.value = _uiState.value.copy(isLoading = false)
    }
}
