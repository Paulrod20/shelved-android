package com.paulrod.shelved.ui.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.paulrod.shelved.ui.theme.Background
import com.paulrod.shelved.ui.auth.EmailAuthMode

private enum class OnboardingPage { WELCOME, FEATURES, ACCOUNT, EMAIL, VERIFY }

@Composable
fun OnboardingScreen(
    state: OnboardingUiState,
    onGoogleSignIn: () -> Unit,
    onCreateAccount: (String, String, String) -> Unit,
    onEmailSignIn: (String, String) -> Unit,
    onPasswordReset: (String) -> Unit,
    onContinueWithoutAccount: () -> Unit,
    onContinueAfterVerification: () -> Unit,
    onResendVerification: () -> Unit,
    onClearFeedback: () -> Unit,
) {
    var page by rememberSaveable { mutableStateOf(OnboardingPage.WELCOME) }
    var emailMode by rememberSaveable { mutableStateOf(EmailAuthMode.CREATE_ACCOUNT) }

    LaunchedEffect(state.verificationEmail) {
        if (state.verificationEmail != null) page = OnboardingPage.VERIFY
    }

    val canGoBack = page in setOf(OnboardingPage.FEATURES, OnboardingPage.ACCOUNT, OnboardingPage.EMAIL)
    BackHandler(enabled = canGoBack && !state.isLoading) {
        page = page.previous()
        onClearFeedback()
    }

    Column(
        Modifier.fillMaxSize().background(Background).systemBarsPadding().imePadding()
            .padding(horizontal = 24.dp, vertical = 12.dp),
    ) {
        OnboardingTopBar(
            progress = page.progress,
            canGoBack = canGoBack,
            onBack = {
                page = page.previous()
                onClearFeedback()
            },
        )
        AnimatedContent(
            targetState = page,
            modifier = Modifier.fillMaxSize(),
            transitionSpec = {
                val direction = if (targetState.ordinal > initialState.ordinal) {
                    AnimatedContentTransitionScope.SlideDirection.Left
                } else {
                    AnimatedContentTransitionScope.SlideDirection.Right
                }
                (slideIntoContainer(direction, tween(280)) + fadeIn(tween(220))) togetherWith
                    (slideOutOfContainer(direction, tween(240)) + fadeOut(tween(180))) using
                    SizeTransform(clip = false)
            },
            label = "onboarding page",
        ) { currentPage ->
            when (currentPage) {
                OnboardingPage.WELCOME -> WelcomePage { page = OnboardingPage.FEATURES }
                OnboardingPage.FEATURES -> FeaturesPage { page = OnboardingPage.ACCOUNT }
                OnboardingPage.ACCOUNT -> AccountPage(
                    state = state,
                    onGoogleSignIn = onGoogleSignIn,
                    onEmail = {
                        onClearFeedback()
                        emailMode = EmailAuthMode.CREATE_ACCOUNT
                        page = OnboardingPage.EMAIL
                    },
                    onExistingAccount = {
                        onClearFeedback()
                        emailMode = EmailAuthMode.SIGN_IN
                        page = OnboardingPage.EMAIL
                    },
                    onContinueWithoutAccount = onContinueWithoutAccount,
                )
                OnboardingPage.EMAIL -> EmailAuthPage(
                    state = state,
                    initialMode = emailMode,
                    onCreateAccount = onCreateAccount,
                    onSignIn = onEmailSignIn,
                    onPasswordReset = onPasswordReset,
                    onClearFeedback = onClearFeedback,
                )
                OnboardingPage.VERIFY -> VerificationPage(
                    state = state,
                    onContinue = onContinueAfterVerification,
                    onResend = onResendVerification,
                )
            }
        }
    }
}

private val OnboardingPage.progress: Int
    get() = when (this) {
        OnboardingPage.WELCOME -> 0
        OnboardingPage.FEATURES -> 1
        else -> 2
    }

private fun OnboardingPage.previous(): OnboardingPage = when (this) {
    OnboardingPage.FEATURES -> OnboardingPage.WELCOME
    OnboardingPage.ACCOUNT -> OnboardingPage.FEATURES
    OnboardingPage.EMAIL -> OnboardingPage.ACCOUNT
    else -> this
}
