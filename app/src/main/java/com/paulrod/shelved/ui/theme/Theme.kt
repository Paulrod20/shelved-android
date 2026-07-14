package com.paulrod.shelved.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ShelvedColors = darkColorScheme(
    primary = Accent,
    onPrimary = AccentText,
    background = Background,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    outline = Border,
)

@Composable
fun ShelvedTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = ShelvedColors, content = content)
}
