package com.travelmate.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

/** Dark-first theme that follows the system setting (plan Section 10.2). */
@Composable
fun TravelMateTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkTravelMateColors else LightTravelMateColors
    CompositionLocalProvider(LocalTravelMateColors provides colors) {
        MaterialTheme(
            colorScheme = colors.toColorScheme(darkTheme),
            typography = TravelMateTypography,
            shapes = TravelMateShapes,
            content = content,
        )
    }
}

object TravelMateTheme {
    /** Extra tokens that do not exist in a Material 3 ColorScheme. */
    val colors: TravelMateColors
        @Composable
        @ReadOnlyComposable
        get() = LocalTravelMateColors.current
}

private fun TravelMateColors.toColorScheme(dark: Boolean): ColorScheme {
    val base = if (dark) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = accentGlow,
        onPrimaryContainer = textPrimary,
        secondary = secondary,
        onSecondary = onPrimary,
        background = bgTop,
        onBackground = textPrimary,
        surface = surface,
        onSurface = textPrimary,
        surfaceVariant = surface,
        onSurfaceVariant = textSecondary,
        surfaceContainerLowest = surface,
        surfaceContainerLow = surface,
        surfaceContainer = surface,
        surfaceContainerHigh = surface,
        surfaceContainerHighest = surface,
        inverseSurface = textPrimary,
        inverseOnSurface = surface,
        outline = outlineGlass,
        outlineVariant = outlineGlass,
        error = error,
        onError = onPrimary,
    )
}
