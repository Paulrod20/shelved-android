package com.paulrod.shelved.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Gamepad
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paulrod.shelved.ui.theme.Accent
import com.paulrod.shelved.ui.theme.Background
import com.paulrod.shelved.ui.theme.TextMuted
import com.paulrod.shelved.ui.theme.TextPrimary

@Composable
fun ShelvedScreen(
    title: String,
    actions: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(Modifier.fillMaxSize().background(Background).statusBarsPadding().padding(horizontal = 16.dp)) {
        Row(
            Modifier.fillMaxWidth().height(64.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                title,
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-.3).sp,
            )
            actions()
        }
        content()
    }
}

@Composable
fun EmptyLibrary(all: Boolean, modifier: Modifier, onAdd: (() -> Unit)? = null) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Outlined.Gamepad, null, tint = TextMuted, modifier = Modifier.size(46.dp))
        Spacer(Modifier.height(14.dp))
        Text(
            if (all) "Your shelf is empty" else "Nothing in this section",
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            if (all) "Add a game to start your collection." else "Try another status.",
            color = TextMuted,
            fontSize = 13.sp,
        )
        if (all && onAdd != null) {
            TextButton(onClick = onAdd) { Text("Add your first game", color = Accent) }
        }
    }
}
