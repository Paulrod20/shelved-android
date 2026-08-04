package com.paulrod.shelved.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.paulrod.shelved.ui.auth.EmailVerificationContent

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
        EmailVerificationContent(
            email = state.verificationEmail.orEmpty(),
            delivery = state.verificationDelivery,
            errorMessage = state.errorMessage,
            noticeMessage = state.noticeMessage,
            isLoading = state.isLoading,
            centered = true,
            onContinue = onContinue,
            onResend = onResend,
        )
    }
}
