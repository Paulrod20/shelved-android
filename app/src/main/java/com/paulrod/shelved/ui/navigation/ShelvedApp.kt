package com.paulrod.shelved.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.paulrod.shelved.ShelvedApplication
import com.paulrod.shelved.data.OnboardingPreferences
import com.paulrod.shelved.ui.onboarding.OnboardingScreen
import com.paulrod.shelved.ui.onboarding.OnboardingViewModel

@Composable
fun ShelvedApp() {
    val activityContext = LocalContext.current
    val appContext = activityContext.applicationContext
    val container = remember(appContext) { (appContext as ShelvedApplication).container }
    val authRepository = container.authRepository
    val googleSignInClient = container.googleSignInClient
    val onboardingViewModel: OnboardingViewModel = viewModel {
        OnboardingViewModel(
            preferences = OnboardingPreferences(appContext),
            authRepository = authRepository,
        )
    }
    val onboardingState by onboardingViewModel.uiState.collectAsStateWithLifecycle()

    if (onboardingState.isCompleted) {
        MainApp(container, activityContext)
    } else {
        OnboardingScreen(
            state = onboardingState,
            onGoogleSignIn = {
                onboardingViewModel.signInWithGoogle {
                    googleSignInClient.getIdToken(activityContext)
                }
            },
            onCreateAccount = onboardingViewModel::createEmailAccount,
            onEmailSignIn = onboardingViewModel::signInWithEmail,
            onPasswordReset = onboardingViewModel::sendPasswordReset,
            onContinueWithoutAccount = onboardingViewModel::continueWithoutAccount,
            onContinueAfterVerification = onboardingViewModel::continueAfterVerification,
            onResendVerification = onboardingViewModel::resendVerification,
            onClearFeedback = onboardingViewModel::clearFeedback,
        )
    }
}
