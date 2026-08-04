package com.paulrod.shelved.ui.auth

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.paulrod.shelved.R
import com.paulrod.shelved.data.auth.VerificationDelivery
import com.paulrod.shelved.ui.components.PageBody
import com.paulrod.shelved.ui.components.PageTitle

/** Shared verification copy, feedback, and actions for every email-auth entry point. */
@Composable
internal fun EmailVerificationContent(
    email: String,
    delivery: VerificationDelivery?,
    errorMessage: AuthMessage?,
    noticeMessage: AuthMessage?,
    isLoading: Boolean,
    centered: Boolean,
    onContinue: () -> Unit,
    onResend: () -> Unit,
) {
    PageTitle(stringResource(R.string.onboarding_verify_title), centered = centered)
    PageBody(
        stringResource(
            when (delivery) {
                VerificationDelivery.SENT -> R.string.onboarding_verify_sent
                VerificationDelivery.SEND_FAILED -> R.string.onboarding_verify_failed
                else -> R.string.onboarding_verify_required
            },
            email,
        ),
        centered = centered,
    )
    Spacer(Modifier.height(20.dp))
    errorMessage?.let { AuthFeedbackCard(it.localized(), isError = true) }
    noticeMessage?.let { AuthFeedbackCard(it.localized(), isError = false) }
    if (errorMessage != null || noticeMessage != null) Spacer(Modifier.height(12.dp))
    AuthPrimaryButton(
        stringResource(R.string.onboarding_continue_to_app),
        enabled = !isLoading,
        onClick = onContinue,
    )
    AuthTextButton(
        stringResource(R.string.onboarding_resend_email),
        enabled = !isLoading,
        onClick = onResend,
    )
}
