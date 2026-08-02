package com.paulrod.shelved.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paulrod.shelved.ui.components.shelvedFieldColors
import com.paulrod.shelved.R
import com.paulrod.shelved.ui.theme.Accent
import com.paulrod.shelved.ui.theme.AccentText
import com.paulrod.shelved.ui.theme.Border
import com.paulrod.shelved.ui.theme.Surface
import com.paulrod.shelved.ui.theme.SurfaceElevated
import com.paulrod.shelved.ui.theme.TextMuted
import com.paulrod.shelved.ui.theme.TextPrimary

private val AuthButtonWidth = 252.dp
private val AuthButtonHeight = 56.dp
private val AuthButtonShape = RoundedCornerShape(28.dp)

@Composable
internal fun OnboardingMessage.localized(): String = stringResource(
    when (this) {
        OnboardingMessage.INVALID_EMAIL -> R.string.onboarding_error_invalid_email
        OnboardingMessage.SHORT_PASSWORD -> R.string.onboarding_error_short_password
        OnboardingMessage.PASSWORD_MISMATCH -> R.string.onboarding_error_password_mismatch
        OnboardingMessage.MISSING_PASSWORD -> R.string.onboarding_error_missing_password
        OnboardingMessage.RESET_EMAIL_REQUIRED -> R.string.onboarding_error_reset_email
        OnboardingMessage.RESET_EMAIL_SENT -> R.string.onboarding_notice_reset_sent
        OnboardingMessage.VERIFICATION_EMAIL_SENT -> R.string.onboarding_notice_verification_sent
        OnboardingMessage.VERIFICATION_SEND_FAILED -> R.string.onboarding_error_verification_send
        OnboardingMessage.GOOGLE_SIGN_IN_FAILED -> R.string.onboarding_error_google
        OnboardingMessage.WEAK_PASSWORD -> R.string.onboarding_error_weak_password
        OnboardingMessage.ACCOUNT_EXISTS -> R.string.onboarding_error_account_exists
        OnboardingMessage.INVALID_CREDENTIALS -> R.string.onboarding_error_invalid_credentials
        OnboardingMessage.NETWORK -> R.string.onboarding_error_network
        OnboardingMessage.AUTH_UNKNOWN -> R.string.onboarding_error_unknown
    },
)

@Composable
internal fun OnboardingScrollablePage(
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable ColumnScope.() -> Unit,
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .heightIn(min = maxHeight),
            horizontalAlignment = horizontalAlignment,
            verticalArrangement = verticalArrangement,
            content = content,
        )
    }
}

@Composable
internal fun OnboardingTopBar(progress: Int, canGoBack: Boolean, onBack: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (canGoBack) {
            IconButton(onClick = onBack, modifier = Modifier.size(44.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.onboarding_back), tint = TextPrimary)
            }
        } else {
            Spacer(Modifier.size(44.dp))
        }
        Row(
            Modifier.weight(1f),
            horizontalArrangement = Arrangement.Center,
        ) {
            repeat(3) { index ->
                Box(
                    Modifier.padding(horizontal = 3.dp).size(width = if (index == progress) 22.dp else 7.dp, height = 7.dp)
                        .clip(CircleShape).background(if (index == progress) Accent else Border),
                )
            }
        }
        Spacer(Modifier.size(44.dp))
    }
}

@Composable
internal fun ShelvedMark(modifier: Modifier = Modifier) {
    Box(
        modifier.size(116.dp).clip(RoundedCornerShape(34.dp)).background(Accent),
        contentAlignment = Alignment.Center,
    ) {
        Text("S", color = AccentText, fontSize = 64.sp, fontWeight = FontWeight.Black, letterSpacing = (-4).sp)
    }
}

@Composable
internal fun VerificationMark(icon: ImageVector) {
    Box(
        Modifier.size(88.dp).clip(RoundedCornerShape(28.dp)).background(Surface),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, tint = Accent, modifier = Modifier.size(38.dp))
    }
}

@Composable
internal fun OnboardingEyebrow(text: String) {
    Text(text, color = Accent, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
    Spacer(Modifier.height(10.dp))
}

@Composable
internal fun OnboardingTitle(text: String, centered: Boolean = false) {
    Text(
        text,
        color = TextPrimary,
        fontSize = 36.sp,
        lineHeight = 40.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-1).sp,
        textAlign = if (centered) TextAlign.Center else TextAlign.Start,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
internal fun OnboardingBody(text: String, centered: Boolean = false) {
    Text(
        text,
        color = TextMuted,
        fontSize = 16.sp,
        lineHeight = 23.sp,
        textAlign = if (centered) TextAlign.Center else TextAlign.Start,
        modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
    )
}

@Composable
internal fun FeatureCard(icon: ImageVector, title: String, body: String) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Surface).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(46.dp).clip(RoundedCornerShape(14.dp)).background(SurfaceElevated),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = Accent, modifier = Modifier.size(24.dp))
        }
        Column(Modifier.weight(1f).padding(start = 14.dp)) {
            Text(title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(body, color = TextMuted, fontSize = 13.sp, lineHeight = 18.sp, modifier = Modifier.padding(top = 3.dp))
        }
    }
}

@Composable
internal fun OnboardingPrimaryButton(
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
internal fun OnboardingGoogleButton(enabled: Boolean, loading: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().height(56.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (loading) {
            Box(
                Modifier.size(width = AuthButtonWidth, height = AuthButtonHeight)
                    .clip(AuthButtonShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(Modifier.size(22.dp), color = Color(0xFF202124), strokeWidth = 2.dp)
            }
        } else {
            Image(
                painter = painterResource(R.drawable.sign_in_with_google),
                contentDescription = stringResource(R.string.onboarding_google_sign_in),
                modifier = Modifier.size(width = AuthButtonWidth, height = AuthButtonHeight)
                    .shadow(2.dp, AuthButtonShape)
                    .alpha(if (enabled) 1f else .55f)
                    .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
            )
        }
    }
}

@Composable
internal fun OnboardingEmailButton(label: String, enabled: Boolean = true, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val active = enabled && isPressed

    Box(
        modifier = Modifier.fillMaxWidth().height(AuthButtonHeight),
        contentAlignment = Alignment.Center,
    ) {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.size(width = AuthButtonWidth, height = AuthButtonHeight),
            shape = AuthButtonShape,
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
internal fun OnboardingTextButton(label: String, enabled: Boolean = true, onClick: () -> Unit) {
    TextButton(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
        Text(label, color = if (enabled) TextMuted else Border, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
internal fun OnboardingFinePrint(text: String) {
    Text(text, color = TextMuted, fontSize = 11.sp, lineHeight = 16.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
}

@Composable
internal fun AuthModeSelector(selected: EmailAuthMode, onSelect: (EmailAuthMode) -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Surface).padding(4.dp)) {
        AuthModeOption(stringResource(R.string.onboarding_create_account), EmailAuthMode.CREATE_ACCOUNT, selected, onSelect)
        AuthModeOption(stringResource(R.string.onboarding_sign_in), EmailAuthMode.SIGN_IN, selected, onSelect)
    }
}

@Composable
private fun RowScope.AuthModeOption(
    label: String,
    mode: EmailAuthMode,
    selected: EmailAuthMode,
    onSelect: (EmailAuthMode) -> Unit,
) {
    Box(
        Modifier.weight(1f).clip(RoundedCornerShape(11.dp))
            .background(if (mode == selected) SurfaceElevated else Color.Transparent)
            .clickable { onSelect(mode) }.padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = if (mode == selected) TextPrimary else TextMuted, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    }
}

@Composable
internal fun OnboardingTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        colors = shelvedFieldColors(),
    )
}

@Composable
internal fun OnboardingPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions,
) {
    var visible by rememberSaveable { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    if (visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                    stringResource(if (visible) R.string.onboarding_hide_password else R.string.onboarding_show_password),
                )
            }
        },
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        colors = shelvedFieldColors(),
    )
}

@Composable
internal fun FeedbackCard(message: String, isError: Boolean) {
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
