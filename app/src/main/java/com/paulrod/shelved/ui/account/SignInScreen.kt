package com.paulrod.shelved.ui.account

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paulrod.shelved.R
import com.paulrod.shelved.ui.auth.AuthFeedbackCard
import com.paulrod.shelved.ui.auth.AuthPasswordField
import com.paulrod.shelved.ui.auth.AuthPrimaryButton
import com.paulrod.shelved.ui.auth.AuthTextButton
import com.paulrod.shelved.ui.auth.AuthTextField
import com.paulrod.shelved.ui.auth.GoogleSignInButton
import com.paulrod.shelved.ui.auth.localized
import com.paulrod.shelved.ui.components.PageBody
import com.paulrod.shelved.ui.components.PageTitle
import com.paulrod.shelved.ui.theme.Background
import com.paulrod.shelved.ui.theme.TextMuted
import com.paulrod.shelved.ui.theme.TextPrimary

@Composable
fun SignInScreen(
    state: AccountUiState,
    onBack: () -> Unit,
    onGoogleSignIn: () -> Unit,
    onEmailSignIn: (String, String) -> Unit,
    onPasswordReset: (String) -> Unit,
    onClearFeedback: () -> Unit,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    BackHandler(enabled = !state.isLoading, onBack = onBack)

    Column(
        Modifier.fillMaxSize().background(Background).systemBarsPadding().imePadding()
            .padding(horizontal = 24.dp),
    ) {
        Row(
            Modifier.fillMaxWidth().height(56.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack, enabled = !state.isLoading, modifier = Modifier.size(44.dp)) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    stringResource(R.string.onboarding_back),
                    tint = TextPrimary,
                )
            }
            Text(
                stringResource(R.string.account_sign_in_header),
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 8.dp),
            )
        }

        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(top = 28.dp, bottom = 28.dp),
        ) {
            PageTitle(stringResource(R.string.account_sign_in_title))
            PageBody(stringResource(R.string.account_sign_in_body))
            Spacer(Modifier.height(30.dp))
            GoogleSignInButton(
                enabled = !state.isLoading,
                loading = state.isLoading,
                onClick = onGoogleSignIn,
            )
            Row(
                Modifier.fillMaxWidth().padding(vertical = 22.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    stringResource(R.string.account_or_email),
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            AuthTextField(
                value = email,
                onValueChange = {
                    email = it
                    onClearFeedback()
                },
                label = stringResource(R.string.onboarding_email),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            )
            Spacer(Modifier.height(12.dp))
            AuthPasswordField(
                value = password,
                onValueChange = {
                    password = it
                    onClearFeedback()
                },
                label = stringResource(R.string.onboarding_password),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onEmailSignIn(email, password) }),
            )
            AuthTextButton(
                stringResource(R.string.onboarding_forgot_password),
                enabled = !state.isLoading,
            ) { onPasswordReset(email) }
            state.errorMessage?.let { AuthFeedbackCard(it.localized(), isError = true) }
            state.noticeMessage?.let { AuthFeedbackCard(it.localized(), isError = false) }
            if (state.errorMessage != null || state.noticeMessage != null) Spacer(Modifier.height(14.dp))
            AuthPrimaryButton(
                label = stringResource(R.string.onboarding_sign_in),
                enabled = !state.isLoading,
                loading = state.isLoading,
            ) { onEmailSignIn(email, password) }
        }
    }
}
