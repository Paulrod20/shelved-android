package com.paulrod.shelved.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.paulrod.shelved.data.OnboardingPreferences
import com.paulrod.shelved.data.auth.AuthRepository
import com.paulrod.shelved.data.auth.GoogleSignInClient
import com.paulrod.shelved.ui.onboarding.OnboardingScreen
import com.paulrod.shelved.ui.onboarding.OnboardingViewModel

@Composable
fun ShelvedApp() {
    val activityContext = LocalContext.current
    val appContext = activityContext.applicationContext
    val authRepository = remember { AuthRepository() }
    val googleSignInClient = remember(appContext) { GoogleSignInClient(appContext) }
    val onboardingViewModel: OnboardingViewModel = viewModel {
        OnboardingViewModel(
            preferences = OnboardingPreferences(appContext),
            authRepository = authRepository,
        )
    }
    val onboardingState by onboardingViewModel.uiState.collectAsStateWithLifecycle()

    if (onboardingState.isCompleted) {
        MainApp(authRepository, authRepository, googleSignInClient, activityContext)
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
