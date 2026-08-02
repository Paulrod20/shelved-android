package com.paulrod.shelved.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.paulrod.shelved.R
import com.paulrod.shelved.ui.auth.AuthPrimaryButton
import com.paulrod.shelved.ui.components.PageBody
import com.paulrod.shelved.ui.components.PageTitle

@Composable
internal fun WelcomePage(onNext: () -> Unit) {
    var entered by rememberSaveable { mutableStateOf(false) }
    val markScale by animateFloatAsState(
        targetValue = if (entered) 1f else .82f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "Shelved mark scale",
    )
    val markAlpha by animateFloatAsState(
        targetValue = if (entered) 1f else 0f,
        animationSpec = tween(420),
        label = "Shelved mark opacity",
    )
    LaunchedEffect(Unit) { entered = true }

    OnboardingScrollablePage(verticalArrangement = Arrangement.SpaceBetween) {
        Column(
            Modifier.fillMaxWidth().height(220.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            ShelvedMark(Modifier.scale(markScale).alpha(markAlpha))
        }
        AnimatedVisibility(
            visible = entered,
            enter = fadeIn(tween(420, delayMillis = 100)) +
                slideInVertically(tween(420, delayMillis = 100)) { it / 5 },
        ) {
            Column {
                OnboardingEyebrow(stringResource(R.string.onboarding_welcome_eyebrow))
                PageTitle(stringResource(R.string.onboarding_welcome_title))
                PageBody(stringResource(R.string.onboarding_welcome_body))
                Spacer(Modifier.height(28.dp))
                AuthPrimaryButton(stringResource(R.string.onboarding_get_started), onClick = onNext)
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}
