package com.apexplayoptimizer.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

// ── AppColors ─────────────────────────────────────────────────────────────────
data class AppColors(
    val background:    Color,
    val surface:       Color,
    val card:          Color,
    val cardBorder:    Color,
    val dividerColor:  Color,
    val primary:       Color,
    val primaryDim:    Color,
    val orange:        Color,
    val orangeDim:     Color,
    val blue:          Color,
    val purple:        Color,
    val yellow:        Color,
    val teal:          Color,
    val success:       Color,
    val danger:        Color,
    val warning:       Color,
    val textPrimary:   Color,
    val textSecondary: Color,
    val textMuted:     Color,
    val gaugeTrack:    Color,
    val sliderTrack:   Color,
)

fun darkAppColors() = AppColors(
    background    = Color(0xFF09090E),
    surface       = Color(0xFF10121C),
    card          = Color(0xFF141620),
    cardBorder    = Color(0xFF1E2132),
    dividerColor  = Color(0xFF181A28),
    primary       = Color(0xFF4C8BF5),
    primaryDim    = Color(0x334C8BF5),
    orange        = Color(0xFFF97316),
    orangeDim     = Color(0x33F97316),
    blue          = Color(0xFF4C8BF5),
    purple        = Color(0xFF8B5CF6),
    yellow        = Color(0xFFF59E0B),
    teal          = Color(0xFF14B8A6),
    success       = Color(0xFF22C55E),
    danger        = Color(0xFFEF4444),
    warning       = Color(0xFFF59E0B),
    textPrimary   = Color(0xFFF1F5F9),
    textSecondary = Color(0xFF8B9DC3),
    textMuted     = Color(0xFF4A5568),
    gaugeTrack    = Color(0xFF1E2132),
    sliderTrack   = Color(0xFF252840),
)

fun lightAppColors() = AppColors(
    background    = Color(0xFFF4F6FA),
    surface       = Color(0xFFFFFFFF),
    card          = Color(0xFFEEF1F8),
    cardBorder    = Color(0xFFD8DFF0),
    dividerColor  = Color(0xFFE5E9F4),
    primary       = Color(0xFF2563EB),
    primaryDim    = Color(0x332563EB),
    orange        = Color(0xFFEA6D0A),
    orangeDim     = Color(0x33EA6D0A),
    blue          = Color(0xFF2563EB),
    purple        = Color(0xFF7C3AED),
    yellow        = Color(0xFFD97706),
    teal          = Color(0xFF0D9488),
    success       = Color(0xFF16A34A),
    danger        = Color(0xFFDC2626),
    warning       = Color(0xFFD97706),
    textPrimary   = Color(0xFF0F172A),
    textSecondary = Color(0xFF475569),
    textMuted     = Color(0xFF94A3B8),
    gaugeTrack    = Color(0xFFDDE3F0),
    sliderTrack   = Color(0xFFCBD5E1),
)

val LocalAppColors = compositionLocalOf { darkAppColors() }

// ── Backward-compatible @Composable property getters ─────────────────────────
// All existing screens use these names — no screen code needs to change.

val Background:    Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.background
val Surface:       Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.surface
val Card:          Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.card
val CardBorder:    Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.cardBorder
val DividerColor:  Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.dividerColor
val Primary:       Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.primary
val PrimaryDim:    Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.primaryDim
val Orange:        Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.orange
val OrangeDim:     Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.orangeDim
val Blue:          Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.blue
val Purple:        Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.purple
val Yellow:        Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.yellow
val Teal:          Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.teal
val Success:       Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.success
val Danger:        Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.danger
val Warning:       Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.warning
val TextPrimary:   Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.textPrimary
val TextSecondary: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.textSecondary
val TextMuted:     Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.textMuted
val GaugeTrack:    Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.gaugeTrack
val SliderTrack:   Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.sliderTrack
