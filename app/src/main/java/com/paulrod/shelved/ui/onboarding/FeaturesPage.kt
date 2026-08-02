package com.paulrod.shelved.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.paulrod.shelved.R
import com.paulrod.shelved.ui.auth.AuthPrimaryButton
import com.paulrod.shelved.ui.components.PageTitle

@Composable
internal fun FeaturesPage(onNext: () -> Unit) {
    OnboardingScrollablePage(verticalArrangement = Arrangement.SpaceBetween) {
        Column {
            Spacer(Modifier.height(26.dp))
            OnboardingEyebrow(stringResource(R.string.onboarding_features_eyebrow))
            PageTitle(stringResource(R.string.onboarding_features_title))
            Spacer(Modifier.height(24.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FeatureCard(
                    Icons.Outlined.CollectionsBookmark,
                    stringResource(R.string.onboarding_library_title),
                    stringResource(R.string.onboarding_library_body),
                )
                FeatureCard(
                    Icons.Outlined.EditNote,
                    stringResource(R.string.onboarding_details_title),
                    stringResource(R.string.onboarding_details_body),
                )
                FeatureCard(
                    Icons.Outlined.BarChart,
                    stringResource(R.string.onboarding_stats_title),
                    stringResource(R.string.onboarding_stats_body),
                )
            }
        }
        Column {
            AuthPrimaryButton(stringResource(R.string.onboarding_continue), onClick = onNext)
            Spacer(Modifier.height(12.dp))
        }
    }
}
