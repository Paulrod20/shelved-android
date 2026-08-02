package com.paulrod.shelved.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paulrod.shelved.R
import com.paulrod.shelved.ui.theme.Accent

@Composable
internal fun AuthMessage.localized(): String = stringResource(
    when (this) {
        AuthMessage.INVALID_EMAIL -> R.string.onboarding_error_invalid_email
        AuthMessage.SHORT_PASSWORD -> R.string.onboarding_error_short_password
        AuthMessage.PASSWORD_MISMATCH -> R.string.onboarding_error_password_mismatch
        AuthMessage.MISSING_PASSWORD -> R.string.onboarding_error_missing_password
        AuthMessage.RESET_EMAIL_REQUIRED -> R.string.onboarding_error_reset_email
        AuthMessage.RESET_EMAIL_SENT -> R.string.onboarding_notice_reset_sent
        AuthMessage.VERIFICATION_EMAIL_SENT -> R.string.onboarding_notice_verification_sent
        AuthMessage.VERIFICATION_SEND_FAILED -> R.string.onboarding_error_verification_send
        AuthMessage.GOOGLE_SIGN_IN_FAILED -> R.string.onboarding_error_google
        AuthMessage.WEAK_PASSWORD -> R.string.onboarding_error_weak_password
        AuthMessage.ACCOUNT_EXISTS -> R.string.onboarding_error_account_exists
        AuthMessage.INVALID_CREDENTIALS -> R.string.onboarding_error_invalid_credentials
        AuthMessage.NETWORK -> R.string.onboarding_error_network
        AuthMessage.UNKNOWN -> R.string.onboarding_error_unknown
    },
)

@Composable
internal fun AuthFeedbackCard(message: String, isError: Boolean) {
    val color = if (isError) Color(0xFFFF8A80) else Accent
    Text(
        message,
        color = color,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = .1f)).padding(horizontal = 14.dp, vertical = 11.dp),
    )
}
