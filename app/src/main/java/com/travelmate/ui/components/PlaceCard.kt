package com.travelmate.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.travelmate.core.Formatters
import com.travelmate.domain.model.Place
import com.travelmate.ui.theme.Spacing
import com.travelmate.ui.theme.TMShapes
import com.travelmate.ui.theme.TravelMateTheme

/** 160 x 200 dp place card: image, name (max 2 lines), "Type · SAR price". */
@Composable
fun PlaceCard(
    place: Place,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TravelMateTheme.colors
    Column(
        modifier = modifier
            .size(width = 160.dp, height = 200.dp)
            .clip(TMShapes.card)
            .background(colors.surfaceGlass)
            .border(1.dp, colors.outlineGlass, TMShapes.card)
            .clickable(onClickLabel = "Show ${place.name}", onClick = onClick),
    ) {
        RemoteImage(
            url = place.imageUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().height(112.dp),
        )
        Column(Modifier.padding(Spacing.md)) {
            Text(
                text = place.name,
                style = MaterialTheme.typography.labelLarge,
                color = colors.textPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(Spacing.xs))
            Text(
                text = "${place.type.label} · ${Formatters.priceOrFree(place.priceSar)}",
                style = MaterialTheme.typography.labelMedium,
                color = colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
