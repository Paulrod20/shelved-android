package com.paulrod.shelved.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.paulrod.shelved.R
import com.paulrod.shelved.ui.auth.AuthFeedbackCard
import com.paulrod.shelved.ui.auth.AuthPasswordField
import com.paulrod.shelved.ui.auth.AuthPrimaryButton
import com.paulrod.shelved.ui.auth.AuthTextButton
import com.paulrod.shelved.ui.auth.AuthTextField
import com.paulrod.shelved.ui.auth.localized
import com.paulrod.shelved.ui.components.PageTitle

@Composable
internal fun EmailAuthPage(
    state: OnboardingUiState,
    initialMode: EmailAuthMode,
    onCreateAccount: (String, String, String) -> Unit,
    onSignIn: (String, String) -> Unit,
    onPasswordReset: (String) -> Unit,
    onClearFeedback: () -> Unit,
) {
    var mode by rememberSaveable(initialMode) { mutableStateOf(initialMode) }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmation by rememberSaveable { mutableStateOf("") }

    OnboardingScrollablePage {
        Spacer(Modifier.height(18.dp))
        OnboardingEyebrow(stringResource(R.string.onboarding_email_eyebrow))
        PageTitle(
            stringResource(
                if (mode == EmailAuthMode.CREATE_ACCOUNT) {
                    R.string.onboarding_create_title
                } else {
                    R.string.onboarding_welcome_back
                },
            ),
        )
        Spacer(Modifier.height(18.dp))
        AuthModeSelector(mode) {
            mode = it
            onClearFeedback()
        }
        Spacer(Modifier.height(18.dp))
        AuthTextField(
            value = email,
            onValueChange = {
                email = it
                onClearFeedback()
            },
            label = stringResource(R.string.onboarding_email),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
        )
        Spacer(Modifier.height(12.dp))
        AuthPasswordField(
            value = password,
            onValueChange = {
                password = it
                onClearFeedback()
            },
            label = stringResource(R.string.onboarding_password),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = if (mode == EmailAuthMode.CREATE_ACCOUNT) ImeAction.Next else ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = { if (mode == EmailAuthMode.SIGN_IN) onSignIn(email, password) },
            ),
        )
        if (mode == EmailAuthMode.CREATE_ACCOUNT) {
            Spacer(Modifier.height(12.dp))
            AuthPasswordField(
                value = confirmation,
                onValueChange = {
                    confirmation = it
                    onClearFeedback()
                },
                label = stringResource(R.string.onboarding_confirm_password),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onCreateAccount(email, password, confirmation) }),
            )
        }
        if (mode == EmailAuthMode.SIGN_IN) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                AuthTextButton(
                    stringResource(R.string.onboarding_forgot_password),
                    enabled = !state.isLoading,
                ) { onPasswordReset(email) }
            }
        } else {
            OnboardingFinePrint(stringResource(R.string.onboarding_password_note))
        }
        Spacer(Modifier.height(16.dp))
        state.errorMessage?.let { AuthFeedbackCard(it.localized(), isError = true) }
        state.noticeMessage?.let { AuthFeedbackCard(it.localized(), isError = false) }
        if (state.errorMessage != null || state.noticeMessage != null) Spacer(Modifier.height(12.dp))
        AuthPrimaryButton(
            label = stringResource(
                if (mode == EmailAuthMode.CREATE_ACCOUNT) {
                    R.string.onboarding_create_account
                } else {
                    R.string.onboarding_sign_in
                },
            ),
            enabled = !state.isLoading,
            loading = state.isLoading,
        ) {
            if (mode == EmailAuthMode.CREATE_ACCOUNT) onCreateAccount(email, password, confirmation)
            else onSignIn(email, password)
        }
        Spacer(Modifier.height(24.dp))
    }
}
