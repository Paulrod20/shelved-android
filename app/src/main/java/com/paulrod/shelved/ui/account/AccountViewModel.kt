package com.paulrod.shelved.ui.account

import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulrod.shelved.data.auth.AuthGateway
import com.paulrod.shelved.data.auth.AuthSessionProvider
import com.paulrod.shelved.data.auth.AuthSession
import com.paulrod.shelved.data.auth.authFailure
import com.paulrod.shelved.ui.auth.AuthMessage
import com.paulrod.shelved.ui.auth.toAuthMessage
import com.paulrod.shelved.ui.auth.validateResetEmail
import com.paulrod.shelved.ui.auth.validateSignIn
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AccountViewModel(
    private val authGateway: AuthGateway,
    sessionProvider: AuthSessionProvider,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AccountUiState(session = sessionProvider.currentSession))
    val uiState: StateFlow<AccountUiState> = _uiState

    init {
        viewModelScope.launch {
            sessionProvider.sessions.collect { session ->
                _uiState.update { it.copy(session = session) }
            }
        }
    }

    fun showSignIn() {
        _uiState.update { it.copy(isSignInVisible = true, errorMessage = null, noticeMessage = null) }
    }

    fun dismissSignIn() {
        if (_uiState.value.isLoading) return
        _uiState.update { it.copy(isSignInVisible = false, errorMessage = null, noticeMessage = null) }
    }

    fun signInWithEmail(email: String, password: String) {
        validateSignIn(email.trim(), password)?.let {
            showError(it)
            return
        }
        launchAuth(
            operation = { authGateway.signInWithEmail(email.trim(), password) },
            onSuccess = { finishSignIn() },
        )
    }

    fun signInWithGoogle(idTokenProvider: suspend () -> String) {
        launchAuth(
            operation = { authGateway.signInWithGoogle(idTokenProvider()) },
            onSuccess = { finishSignIn() },
            onError = { error ->
                if (error is GetCredentialCancellationException) clearLoading()
                else showError(AuthMessage.GOOGLE_SIGN_IN_FAILED)
            },
        )
    }

    fun sendPasswordReset(email: String) {
        validateResetEmail(email.trim())?.let {
            showError(it)
            return
        }
        launchAuth(
            operation = { authGateway.sendPasswordReset(email.trim()) },
            onSuccess = {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = null,
                        noticeMessage = AuthMessage.RESET_EMAIL_SENT,
                    )
                }
            },
        )
    }

    fun signOut() {
        authGateway.signOut()
        _uiState.update {
            it.copy(
                session = AuthSession(),
                isSignInVisible = false,
                isLoading = false,
                errorMessage = null,
                noticeMessage = null,
            )
        }
    }

    fun clearFeedback() {
        _uiState.update { it.copy(errorMessage = null, noticeMessage = null) }
    }

    private fun finishSignIn() {
        _uiState.update {
            it.copy(
                isSignInVisible = false,
                isLoading = false,
                errorMessage = null,
                noticeMessage = null,
            )
        }
    }

    private fun <T> launchAuth(
        operation: suspend () -> T,
        onSuccess: (T) -> Unit,
        onError: (Throwable) -> Unit = { showError(it.authFailure().toAuthMessage()) },
    ) {
        if (_uiState.value.isLoading) return
        _uiState.update { it.copy(isLoading = true, errorMessage = null, noticeMessage = null) }
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
        _uiState.update { it.copy(isLoading = false, errorMessage = message, noticeMessage = null) }
    }

    private fun clearLoading() {
        _uiState.update { it.copy(isLoading = false) }
    }
}
