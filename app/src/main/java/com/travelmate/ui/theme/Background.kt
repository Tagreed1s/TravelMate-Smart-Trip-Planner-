package com.travelmate.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Ambient background used at the root of every screen: a vertical gradient bgTop -> bgBottom
 * plus one soft radial glow of `primary` at 10 percent alpha in the top-left corner.
 */
@Composable
fun AmbientBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val colors = TravelMateTheme.colors
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(colors.bgTop, colors.bgBottom)))
            .drawBehind {
                drawRect(
                    Brush.radialGradient(
                        colors = listOf(colors.primary.copy(alpha = 0.10f), Color.Transparent),
                        center = Offset(size.width * 0.1f, size.height * 0.05f),
                        radius = size.width * 0.9f,
                    ),
                )
            },
        content = content,
    )
}
