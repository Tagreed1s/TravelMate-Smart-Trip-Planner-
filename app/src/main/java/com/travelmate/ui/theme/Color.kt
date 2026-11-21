package com.travelmate.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** All TravelMate color tokens (plan Section 10.3). Access with `TravelMateTheme.colors`. */
@Immutable
data class TravelMateColors(
    val bgTop: Color,
    val bgBottom: Color,
    val surface: Color,
    val surfaceGlass: Color,
    val outlineGlass: Color,
    val primary: Color,
    val primaryEnd: Color,
    val onPrimary: Color,
    val secondary: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val success: Color,
    val warning: Color,
    val error: Color,
) {
    /** Fill for active or selected elements ("accent glow"). */
    val accentGlow: Color get() = primary.copy(alpha = 0.14f)
}

val DarkTravelMateColors = TravelMateColors(
    bgTop = Color(0xFF0B1220),
    bgBottom = Color(0xFF0E1A2E),
    surface = Color(0xFF121A2B),
    surfaceGlass = Color(0xFF121A2B).copy(alpha = 0.72f),
    outlineGlass = Color.White.copy(alpha = 0.08f),
    primary = Color(0xFF2DD4BF),
    primaryEnd = Color(0xFF38BDF8),
    onPrimary = Color(0xFF04211E),
    secondary = Color(0xFF7DD3FC),
    textPrimary = Color(0xFFE6EDF7),
    textSecondary = Color(0xFF93A1B8),
    success = Color(0xFF34D399),
    warning = Color(0xFFFBBF24),
    error = Color(0xFFF87171),
)

val LightTravelMateColors = TravelMateColors(
    bgTop = Color(0xFFF6F8FB),
    bgBottom = Color(0xFFEEF2F7),
    surface = Color(0xFFFFFFFF),
    surfaceGlass = Color.White.copy(alpha = 0.80f),
    outlineGlass = Color(0xFF0B1220).copy(alpha = 0.06f),
    primary = Color(0xFF0D9488),
    primaryEnd = Color(0xFF0284C7),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF0369A1),
    textPrimary = Color(0xFF0B1220),
    textSecondary = Color(0xFF556274),
    success = Color(0xFF059669),
    warning = Color(0xFFB45309),
    error = Color(0xFFDC2626),
)

val LocalTravelMateColors = staticCompositionLocalOf { DarkTravelMateColors }
