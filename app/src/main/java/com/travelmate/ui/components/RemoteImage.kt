package com.travelmate.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.travelmate.ui.theme.PreviewSurface
import com.travelmate.ui.theme.TMShapes
import com.travelmate.ui.theme.ThemePreviews
import com.travelmate.ui.theme.TravelMateTheme

/**
 * Network image with crossfade. While loading, and when the URL is empty or fails,
 * a gradient surface is shown (the failed state adds an image icon).
 */
@Composable
fun RemoteImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val colors = TravelMateTheme.colors
    var failed by remember(url) { mutableStateOf(url.isBlank()) }
    Box(
        modifier = modifier.background(
            Brush.linearGradient(listOf(colors.surface, colors.surfaceGlass, colors.accentGlow)),
        ),
        contentAlignment = Alignment.Center,
    ) {
        if (url.isNotBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(url).crossfade(true).build(),
                contentDescription = contentDescription,
                contentScale = contentScale,
                onSuccess = { failed = false },
                onError = { failed = true },
                modifier = Modifier.matchParentSize(),
            )
        }
        if (failed) {
            Icon(Icons.Rounded.Image, contentDescription = null, tint = colors.textSecondary.copy(alpha = 0.5f))
        }
    }
}

@ThemePreviews
@Composable
private fun RemoteImagePreview() = PreviewSurface {
    RemoteImage(url = "", contentDescription = null, modifier = Modifier.fillMaxWidth().height(120.dp).clip(TMShapes.card))
}
