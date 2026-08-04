package com.paulrod.shelved.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.paulrod.shelved.R
import com.paulrod.shelved.data.model.Game
import com.paulrod.shelved.ui.theme.Accent
import com.paulrod.shelved.ui.theme.TextMuted

@Composable
internal fun StarRatingDisplay(
    rating: Int,
    modifier: Modifier = Modifier,
    starSize: Dp = 13.dp,
) {
    val description = stringResource(R.string.review_rating_display, rating, Game.MAX_RATING)
    Row(modifier.semantics { contentDescription = description }) {
        RatingStars(rating) { filled ->
            Icon(
                imageVector = if (filled) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = null,
                tint = if (filled) Accent else TextMuted,
                modifier = Modifier.size(starSize),
            )
        }
    }
}

@Composable
internal fun StarRatingPicker(
    rating: Int?,
    onRatingChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier) {
        RatingStars(rating ?: 0) { filled, value ->
            IconButton(
                onClick = { onRatingChange(value) },
                modifier = Modifier.size(44.dp),
            ) {
                Icon(
                    imageVector = if (filled) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = stringResource(
                        R.string.review_rate_stars,
                        value,
                        Game.MAX_RATING,
                    ),
                    tint = if (filled) Accent else TextMuted,
                    modifier = Modifier.size(30.dp),
                )
            }
        }
    }
}

@Composable
private fun RatingStars(
    rating: Int,
    content: @Composable (filled: Boolean) -> Unit,
) {
    repeat(Game.MAX_RATING) { index -> content(index < rating) }
}

@Composable
private fun RatingStars(
    rating: Int,
    content: @Composable (filled: Boolean, value: Int) -> Unit,
) {
    repeat(Game.MAX_RATING) { index ->
        val value = index + Game.MIN_RATING
        content(value <= rating, value)
    }
}
