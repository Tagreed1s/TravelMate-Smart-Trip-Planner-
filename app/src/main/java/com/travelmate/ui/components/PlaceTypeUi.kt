package com.travelmate.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.Hiking
import androidx.compose.material.icons.rounded.Museum
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.ui.graphics.vector.ImageVector
import com.travelmate.domain.model.PlaceType

/** Display text and icons for place types (kept out of the domain layer). */
val PlaceType.label: String
    get() = when (this) {
        PlaceType.ATTRACTION -> "Attraction"
        PlaceType.RESTAURANT -> "Restaurant"
        PlaceType.ACTIVITY -> "Activity"
        PlaceType.EVENT -> "Event"
    }

val PlaceType.pluralLabel: String
    get() = when (this) {
        PlaceType.ATTRACTION -> "Attractions"
        PlaceType.RESTAURANT -> "Restaurants"
        PlaceType.ACTIVITY -> "Activities"
        PlaceType.EVENT -> "Events"
    }

val PlaceType.icon: ImageVector
    get() = when (this) {
        PlaceType.ATTRACTION -> Icons.Rounded.Museum
        PlaceType.RESTAURANT -> Icons.Rounded.Restaurant
        PlaceType.ACTIVITY -> Icons.Rounded.Hiking
        PlaceType.EVENT -> Icons.Rounded.Celebration
    }
