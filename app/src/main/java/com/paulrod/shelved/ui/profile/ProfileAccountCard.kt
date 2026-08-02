package com.paulrod.shelved.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paulrod.shelved.R
import com.paulrod.shelved.data.auth.AuthSession
import com.paulrod.shelved.ui.components.PrimaryButton
import com.paulrod.shelved.ui.theme.Surface
import com.paulrod.shelved.ui.theme.TextMuted
import com.paulrod.shelved.ui.theme.TextPrimary

@Composable
internal fun ProfileAccountCard(
    session: AuthSession,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit,
) {
    Column(
        Modifier.fillMaxWidth().padding(bottom = 28.dp).clip(RoundedCornerShape(18.dp))
            .background(Surface).padding(16.dp),
    ) {
        if (session.isSignedIn) {
            SignedInAccount(session, onSignOut)
        } else {
            LocalAccount(onSignIn)
        }
    }
}

@Composable
private fun SignedInAccount(session: AuthSession, onSignOut: () -> Unit) {
    Text(
        stringResource(R.string.account_signed_in_as),
        color = TextMuted,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
    )
    Text(
        session.email ?: session.displayName.orEmpty(),
        color = TextPrimary,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 4.dp),
    )
    if (!session.isEmailVerified && session.email != null) {
        Text(
            stringResource(R.string.account_verification_pending),
            color = TextMuted,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 5.dp),
        )
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        TextButton(onClick = onSignOut) {
            Text(stringResource(R.string.account_sign_out), color = TextMuted, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun LocalAccount(onSignIn: () -> Unit) {
    Text(
        stringResource(R.string.account_local_title),
        color = TextPrimary,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
    )
    Text(
        stringResource(R.string.account_local_body),
        color = TextMuted,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        modifier = Modifier.padding(top = 5.dp, bottom = 14.dp),
    )
    PrimaryButton(stringResource(R.string.account_sign_in), onClick = onSignIn)
}
