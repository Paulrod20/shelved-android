package com.paulrod.shelved.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paulrod.shelved.R
import com.paulrod.shelved.ui.theme.Accent
import com.paulrod.shelved.ui.theme.AccentText
import com.paulrod.shelved.ui.theme.Border
import com.paulrod.shelved.ui.theme.Surface
import com.paulrod.shelved.ui.theme.SurfaceElevated
import com.paulrod.shelved.ui.theme.TextMuted
import com.paulrod.shelved.ui.theme.TextPrimary

private val ProviderButtonWidth = 252.dp
private val ProviderButtonHeight = 56.dp
private val ProviderButtonShape = RoundedCornerShape(28.dp)

@Composable
internal fun AuthPrimaryButton(
    label: String,
    enabled: Boolean = true,
    loading: Boolean = false,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Accent,
            contentColor = AccentText,
            disabledContainerColor = Border,
        ),
    ) {
        if (loading) CircularProgressIndicator(Modifier.size(22.dp), color = AccentText, strokeWidth = 2.dp)
        else Text(label, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}

@Composable
internal fun GoogleSignInButton(enabled: Boolean, loading: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().height(ProviderButtonHeight),
        contentAlignment = Alignment.Center,
    ) {
        if (loading) {
            Box(
                Modifier.size(width = ProviderButtonWidth, height = ProviderButtonHeight)
                    .clip(ProviderButtonShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(Modifier.size(22.dp), color = Color(0xFF202124), strokeWidth = 2.dp)
            }
        } else {
            Image(
                painter = painterResource(R.drawable.sign_in_with_google),
                contentDescription = stringResource(R.string.onboarding_google_sign_in),
                modifier = Modifier.size(width = ProviderButtonWidth, height = ProviderButtonHeight)
                    .shadow(2.dp, ProviderButtonShape)
                    .alpha(if (enabled) 1f else .55f)
                    .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
            )
        }
    }
}

@Composable
internal fun EmailAccountButton(label: String, enabled: Boolean = true, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val active = enabled && isPressed

    Box(
        modifier = Modifier.fillMaxWidth().height(ProviderButtonHeight),
        contentAlignment = Alignment.Center,
    ) {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.size(width = ProviderButtonWidth, height = ProviderButtonHeight),
            shape = ProviderButtonShape,
            interactionSource = interactionSource,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (active) Accent else Surface,
                contentColor = if (active) AccentText else TextPrimary,
                disabledContainerColor = SurfaceElevated,
            ),
        ) {
            Icon(
                imageVector = Icons.Outlined.Email,
                contentDescription = null,
                modifier = Modifier.size(21.dp),
                tint = if (active) AccentText else Accent,
            )
            Spacer(Modifier.size(10.dp))
            Text(label, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        }
    }
}

@Composable
internal fun AuthTextButton(label: String, enabled: Boolean = true, onClick: () -> Unit) {
    TextButton(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
        Text(label, color = if (enabled) TextMuted else Border, fontWeight = FontWeight.SemiBold)
    }
}
