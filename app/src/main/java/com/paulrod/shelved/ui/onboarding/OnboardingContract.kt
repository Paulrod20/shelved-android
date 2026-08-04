package com.paulrod.shelved.ui.onboarding

import com.paulrod.shelved.data.auth.VerificationDelivery
import com.paulrod.shelved.ui.auth.AuthMessage

data class OnboardingUiState(
    val isCompleted: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: AuthMessage? = null,
    val noticeMessage: AuthMessage? = null,
    val verificationEmail: String? = null,
    val verificationDelivery: VerificationDelivery? = null,
)
