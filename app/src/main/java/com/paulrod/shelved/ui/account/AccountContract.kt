package com.paulrod.shelved.ui.account

import com.paulrod.shelved.data.auth.AuthSession
import com.paulrod.shelved.data.auth.VerificationDelivery
import com.paulrod.shelved.ui.auth.AuthMessage

data class AccountUiState(
    val session: AuthSession = AuthSession(),
    val isSignInVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: AuthMessage? = null,
    val noticeMessage: AuthMessage? = null,
    val verificationEmail: String? = null,
    val verificationDelivery: VerificationDelivery? = null,
)
