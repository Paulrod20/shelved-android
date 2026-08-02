package com.paulrod.shelved.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.paulrod.shelved.R
import com.paulrod.shelved.data.model.Profile
import com.paulrod.shelved.ui.theme.Accent
import com.paulrod.shelved.ui.theme.Surface
import com.paulrod.shelved.ui.theme.TextPrimary
import java.io.File

@Composable
internal fun ProfileAvatar(
    profile: Profile,
    modifier: Modifier = Modifier,
    size: Dp = 90.dp,
) {
    Box(
        modifier.size(size).clip(CircleShape).background(Surface).border(2.dp, Accent, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            profile.displayName.firstOrNull()?.uppercase() ?: "🎮",
            color = TextPrimary,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
        )
        profile.profileImagePath?.let { path ->
            AsyncImage(
                model = File(path),
                contentDescription = stringResource(R.string.profile_picture_description),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(CircleShape),
            )
        }
    }
}
