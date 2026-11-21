package com.travelmate.ui.theme

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

/** Renders a preview in dark and light mode. */
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
annotation class ThemePreviews

/** Theme + ambient background for previews. */
@Composable
fun PreviewSurface(content: @Composable () -> Unit) {
    TravelMateTheme {
        AmbientBackground {
            Box(Modifier.padding(Spacing.lg)) { content() }
        }
    }
}
