package com.apexplayoptimizer.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary          = Primary,
    secondary        = Orange,
    tertiary         = Blue,
    background       = Background,
    surface          = Surface,
    onPrimary        = Color.Black,
    onSecondary      = Color.Black,
    onBackground     = TextPrimary,
    onSurface        = TextPrimary,
    surfaceVariant   = Card,
    outline          = CardBorder,
)

@Composable
fun ApexPlayTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography  = Typography,
        content     = content
    )
}
