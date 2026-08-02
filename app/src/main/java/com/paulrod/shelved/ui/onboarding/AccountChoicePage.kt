package com.paulrod.shelved.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.paulrod.shelved.R
import com.paulrod.shelved.ui.auth.AuthFeedbackCard
import com.paulrod.shelved.ui.auth.AuthTextButton
import com.paulrod.shelved.ui.auth.EmailAccountButton
import com.paulrod.shelved.ui.auth.GoogleSignInButton
import com.paulrod.shelved.ui.auth.localized
import com.paulrod.shelved.ui.components.PageBody
import com.paulrod.shelved.ui.components.PageTitle

@Composable
internal fun AccountPage(
    state: OnboardingUiState,
    onGoogleSignIn: () -> Unit,
    onEmail: () -> Unit,
    onExistingAccount: () -> Unit,
    onContinueWithoutAccount: () -> Unit,
) {
    OnboardingScrollablePage(verticalArrangement = Arrangement.SpaceBetween) {
        Column {
            Spacer(Modifier.height(34.dp))
            OnboardingEyebrow(stringResource(R.string.onboarding_account_eyebrow))
            PageTitle(stringResource(R.string.onboarding_account_title))
            PageBody(stringResource(R.string.onboarding_account_body))
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            state.errorMessage?.let { AuthFeedbackCard(it.localized(), isError = true) }
            GoogleSignInButton(!state.isLoading, state.isLoading, onGoogleSignIn)
            EmailAccountButton(
                stringResource(R.string.onboarding_continue_email),
                enabled = !state.isLoading,
                onClick = onEmail,
            )
            Column(verticalArrangement = Arrangement.spacedBy((-8).dp)) {
                AuthTextButton(
                    stringResource(R.string.onboarding_existing_account),
                    enabled = !state.isLoading,
                    onClick = onExistingAccount,
                )
                AuthTextButton(
                    stringResource(R.string.onboarding_continue_local),
                    enabled = !state.isLoading,
                    onClick = onContinueWithoutAccount,
                )
            }
            OnboardingFinePrint(stringResource(R.string.onboarding_local_note))
            Spacer(Modifier.height(4.dp))
        }
    }
}
