package com.paulrod.shelved.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.paulrod.shelved.R
import com.paulrod.shelved.data.auth.VerificationDelivery
import com.paulrod.shelved.ui.auth.AuthFeedbackCard
import com.paulrod.shelved.ui.auth.AuthPrimaryButton
import com.paulrod.shelved.ui.auth.AuthTextButton
import com.paulrod.shelved.ui.auth.localized
import com.paulrod.shelved.ui.components.PageBody
import com.paulrod.shelved.ui.components.PageTitle

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
        PageTitle(stringResource(R.string.onboarding_verify_title), centered = true)
        PageBody(
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
        state.errorMessage?.let { AuthFeedbackCard(it.localized(), isError = true) }
        state.noticeMessage?.let { AuthFeedbackCard(it.localized(), isError = false) }
        if (state.errorMessage != null || state.noticeMessage != null) Spacer(Modifier.height(12.dp))
        AuthPrimaryButton(
            stringResource(R.string.onboarding_continue_to_app),
            enabled = !state.isLoading,
            onClick = onContinue,
        )
        AuthTextButton(
            stringResource(R.string.onboarding_resend_email),
            enabled = !state.isLoading,
            onClick = onResend,
        )
    }
}
