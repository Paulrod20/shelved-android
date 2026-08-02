package com.paulrod.shelved.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Email
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.paulrod.shelved.R
import com.paulrod.shelved.data.auth.VerificationDelivery

@Composable
internal fun WelcomePage(onNext: () -> Unit) {
    var entered by rememberSaveable { mutableStateOf(false) }
    val markScale by animateFloatAsState(
        targetValue = if (entered) 1f else .82f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "Shelved mark scale",
    )
    val markAlpha by animateFloatAsState(
        targetValue = if (entered) 1f else 0f,
        animationSpec = tween(420),
        label = "Shelved mark opacity",
    )
    LaunchedEffect(Unit) { entered = true }

    OnboardingScrollablePage(verticalArrangement = Arrangement.SpaceBetween) {
        Column(
            Modifier.fillMaxWidth().height(220.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            ShelvedMark(Modifier.scale(markScale).alpha(markAlpha))
        }
        AnimatedVisibility(
            visible = entered,
            enter = fadeIn(tween(420, delayMillis = 100)) + slideInVertically(tween(420, delayMillis = 100)) { it / 5 },
        ) {
            Column {
                OnboardingEyebrow(stringResource(R.string.onboarding_welcome_eyebrow))
                OnboardingTitle(stringResource(R.string.onboarding_welcome_title))
                OnboardingBody(stringResource(R.string.onboarding_welcome_body))
                Spacer(Modifier.height(28.dp))
                OnboardingPrimaryButton(stringResource(R.string.onboarding_get_started), onClick = onNext)
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
internal fun FeaturesPage(onNext: () -> Unit) {
    OnboardingScrollablePage(verticalArrangement = Arrangement.SpaceBetween) {
        Column {
            Spacer(Modifier.height(26.dp))
            OnboardingEyebrow(stringResource(R.string.onboarding_features_eyebrow))
            OnboardingTitle(stringResource(R.string.onboarding_features_title))
            Spacer(Modifier.height(24.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FeatureCard(Icons.Outlined.CollectionsBookmark, stringResource(R.string.onboarding_library_title), stringResource(R.string.onboarding_library_body))
                FeatureCard(Icons.Outlined.EditNote, stringResource(R.string.onboarding_details_title), stringResource(R.string.onboarding_details_body))
                FeatureCard(Icons.Outlined.BarChart, stringResource(R.string.onboarding_stats_title), stringResource(R.string.onboarding_stats_body))
            }
        }
        Column {
            OnboardingPrimaryButton(stringResource(R.string.onboarding_continue), onClick = onNext)
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
internal fun AccountPage(
    state: OnboardingUiState,
    onGoogleSignIn: () -> Unit,
    onEmail: () -> Unit,
    onContinueWithoutAccount: () -> Unit,
) {
    OnboardingScrollablePage(verticalArrangement = Arrangement.SpaceBetween) {
        Column {
            Spacer(Modifier.height(34.dp))
            OnboardingEyebrow(stringResource(R.string.onboarding_account_eyebrow))
            OnboardingTitle(stringResource(R.string.onboarding_account_title))
            OnboardingBody(stringResource(R.string.onboarding_account_body))
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            state.errorMessage?.let { FeedbackCard(it.localized(), isError = true) }
            OnboardingGoogleButton(enabled = !state.isLoading, loading = state.isLoading, onClick = onGoogleSignIn)
            OnboardingEmailButton(stringResource(R.string.onboarding_continue_email), enabled = !state.isLoading, onClick = onEmail)
            OnboardingTextButton(stringResource(R.string.onboarding_continue_local), enabled = !state.isLoading, onClick = onContinueWithoutAccount)
            OnboardingFinePrint(stringResource(R.string.onboarding_local_note))
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
internal fun EmailAuthPage(
    state: OnboardingUiState,
    onCreateAccount: (String, String, String) -> Unit,
    onSignIn: (String, String) -> Unit,
    onPasswordReset: (String) -> Unit,
    onClearFeedback: () -> Unit,
) {
    var mode by rememberSaveable { mutableStateOf(EmailAuthMode.CREATE_ACCOUNT) }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmation by rememberSaveable { mutableStateOf("") }

    OnboardingScrollablePage {
        Spacer(Modifier.height(18.dp))
        OnboardingEyebrow(stringResource(R.string.onboarding_email_eyebrow))
        OnboardingTitle(stringResource(if (mode == EmailAuthMode.CREATE_ACCOUNT) R.string.onboarding_create_title else R.string.onboarding_welcome_back))
        Spacer(Modifier.height(18.dp))
        AuthModeSelector(mode) {
            mode = it
            onClearFeedback()
        }
        Spacer(Modifier.height(18.dp))
        OnboardingTextField(
            value = email,
            onValueChange = {
                email = it
                onClearFeedback()
            },
            label = stringResource(R.string.onboarding_email),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
        )
        Spacer(Modifier.height(12.dp))
        OnboardingPasswordField(
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
            OnboardingPasswordField(
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
                OnboardingTextButton(stringResource(R.string.onboarding_forgot_password), enabled = !state.isLoading) { onPasswordReset(email) }
            }
        } else {
            OnboardingFinePrint(stringResource(R.string.onboarding_password_note))
        }
        Spacer(Modifier.height(16.dp))
        state.errorMessage?.let { FeedbackCard(it.localized(), isError = true) }
        state.noticeMessage?.let { FeedbackCard(it.localized(), isError = false) }
        if (state.errorMessage != null || state.noticeMessage != null) Spacer(Modifier.height(12.dp))
        OnboardingPrimaryButton(
            label = stringResource(if (mode == EmailAuthMode.CREATE_ACCOUNT) R.string.onboarding_create_account else R.string.onboarding_sign_in),
            enabled = !state.isLoading,
            loading = state.isLoading,
        ) {
            if (mode == EmailAuthMode.CREATE_ACCOUNT) onCreateAccount(email, password, confirmation)
            else onSignIn(email, password)
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
internal fun VerificationPage(
    state: OnboardingUiState,
    onContinue: () -> Unit,
    onResend: () -> Unit,
) {
    OnboardingScrollablePage(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        VerificationMark(Icons.Outlined.Email)
        Spacer(Modifier.height(28.dp))
        OnboardingTitle(stringResource(R.string.onboarding_verify_title), centered = true)
        OnboardingBody(
            stringResource(
                when (state.verificationDelivery) {
                    VerificationDelivery.SENT -> R.string.onboarding_verify_sent
                    VerificationDelivery.SEND_FAILED -> R.string.onboarding_verify_failed
                    else -> R.string.onboarding_verify_required
                },
                state.verificationEmail.orEmpty(),
            ),
            centered = true,
        )
        Spacer(Modifier.height(20.dp))
        state.errorMessage?.let { FeedbackCard(it.localized(), isError = true) }
        state.noticeMessage?.let { FeedbackCard(it.localized(), isError = false) }
        if (state.errorMessage != null || state.noticeMessage != null) Spacer(Modifier.height(12.dp))
        OnboardingPrimaryButton(stringResource(R.string.onboarding_continue_to_app), enabled = !state.isLoading, onClick = onContinue)
        OnboardingTextButton(stringResource(R.string.onboarding_resend_email), enabled = !state.isLoading, onClick = onResend)
    }
}
