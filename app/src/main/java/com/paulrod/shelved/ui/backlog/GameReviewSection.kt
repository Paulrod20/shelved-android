package com.paulrod.shelved.ui.backlog

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paulrod.shelved.R
import com.paulrod.shelved.ui.components.SectionLabel
import com.paulrod.shelved.ui.components.ShelvedField
import com.paulrod.shelved.ui.components.StarRatingPicker
import com.paulrod.shelved.ui.theme.Accent
import com.paulrod.shelved.ui.theme.TextMuted

@Composable
internal fun GameReviewSection(
    rating: Int?,
    review: String,
    onRatingChange: (Int?) -> Unit,
    onReviewChange: (String) -> Unit,
) {
    SectionLabel(stringResource(R.string.review_section))
    Text(
        stringResource(R.string.review_private_note),
        color = TextMuted,
        fontSize = 12.sp,
        modifier = Modifier.padding(bottom = 4.dp),
    )
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StarRatingPicker(
            rating = rating,
            onRatingChange = { onRatingChange(it) },
            modifier = Modifier.offset(x = (-7).dp),
        )
        if (rating != null) {
            TextButton(onClick = { onRatingChange(null) }) {
                Text(
                    stringResource(R.string.review_clear_rating),
                    color = Accent,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
    ShelvedField(
        value = review,
        onChange = onReviewChange,
        placeholder = stringResource(R.string.review_placeholder),
        minLines = 4,
    )
}
