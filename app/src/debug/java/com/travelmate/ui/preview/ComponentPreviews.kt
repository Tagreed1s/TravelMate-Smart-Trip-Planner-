package com.travelmate.ui.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import com.travelmate.domain.model.PlaceType
import com.travelmate.ui.components.DestinationCard
import com.travelmate.ui.components.PlaceCard
import com.travelmate.ui.components.TimelineItem
import com.travelmate.ui.screens.destination.PlaceDetails
import com.travelmate.ui.theme.PreviewSurface
import com.travelmate.ui.theme.Spacing
import com.travelmate.ui.theme.ThemePreviews

@ThemePreviews
@Composable
private fun DestinationCardPreview() = PreviewSurface {
    DestinationCard(destination = PreviewData.destinationA, onClick = {}, onPlan = {})
}

@ThemePreviews
@Composable
private fun PlaceCardPreview() = PreviewSurface {
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
        PlaceCard(place = PreviewData.places[0], onClick = {})
        PlaceCard(place = PreviewData.places[3], onClick = {})
    }
}

@ThemePreviews
@Composable
private fun TimelineItemPreview() = PreviewSurface {
    Column {
        TimelineItem(startMin = 540, endMin = 600, name = "SAMPLE Place 1", type = PlaceType.ATTRACTION, priceSar = 10, imageUrl = "", onRemove = {})
        TimelineItem(startMin = 660, endMin = 720, name = "SAMPLE Place 2", type = PlaceType.RESTAURANT, priceSar = 20, imageUrl = "", showLine = false)
    }
}

@ThemePreviews
@Composable
private fun PlaceDetailsPreview() = PreviewSurface {
    PlaceDetails(place = PreviewData.places[2], isFavorite = true, onToggleFavorite = {})
}
