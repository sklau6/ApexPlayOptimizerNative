package com.apexplayoptimizer.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

@Composable
fun ApexPlayTheme(isDark: Boolean = true, content: @Composable () -> Unit) {
    val appColors = if (isDark) darkAppColors() else lightAppColors()

    val materialColors = if (isDark) {
        darkColorScheme(
            primary        = appColors.primary,
            secondary      = appColors.orange,
            tertiary       = appColors.blue,
            background     = appColors.background,
            surface        = appColors.surface,
            onPrimary      = Color.Black,
            onSecondary    = Color.Black,
            onBackground   = appColors.textPrimary,
            onSurface      = appColors.textPrimary,
            surfaceVariant = appColors.card,
            outline        = appColors.cardBorder,
        )
    } else {
        lightColorScheme(
            primary        = appColors.primary,
            secondary      = appColors.orange,
            tertiary       = appColors.blue,
            background     = appColors.background,
            surface        = appColors.surface,
            onPrimary      = Color.White,
            onSecondary    = Color.White,
            onBackground   = appColors.textPrimary,
            onSurface      = appColors.textPrimary,
            surfaceVariant = appColors.card,
            outline        = appColors.cardBorder,
        )
    }

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = materialColors,
            typography  = Typography,
            content     = content
        )
    }
}
