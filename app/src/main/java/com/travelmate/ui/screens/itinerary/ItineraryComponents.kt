package com.travelmate.ui.screens.itinerary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.travelmate.domain.model.ItineraryItem
import com.travelmate.domain.model.Place
import com.travelmate.ui.components.TimelineItem
import com.travelmate.ui.theme.Spacing
import com.travelmate.ui.theme.TMShapes
import com.travelmate.ui.theme.TravelMateTheme

/**
 * The selected day's items. Remove an item with its delete icon or by swiping it from end to start.
 * [footer] is drawn after the last item (the Add activity button).
 */
@Composable
fun DayTimeline(
    items: List<ItineraryItem>,
    placesById: Map<String, Place>,
    onRemove: (String) -> Unit,
    contentPadding: PaddingValues,
    footer: @Composable () -> Unit = {},
) {
    val haptics = LocalHapticFeedback.current
    LazyColumn(contentPadding = contentPadding) {
        itemsIndexed(items, key = { _, item -> item.id }) { index, item ->
            val place = placesById[item.placeId]
            val remove = {
                haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                onRemove(item.id)
            }
            SwipeToRemove(onRemove = remove, modifier = Modifier.animateItem()) {
                TimelineItem(
                    startMin = item.startMin,
                    endMin = item.endMin,
                    name = place?.name ?: "Place no longer in the catalog",
                    type = item.type,
                    priceSar = item.priceSar,
                    imageUrl = place?.imageUrl.orEmpty(),
                    showLine = index < items.lastIndex,
                    onRemove = remove,
                )
            }
        }
        item(key = "footer") { footer() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToRemove(
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val colors = TravelMateTheme.colors
    val state = rememberSwipeToDismissBoxState()
    val currentOnRemove by rememberUpdatedState(onRemove)
    LaunchedEffect(state.currentValue) {
        if (state.currentValue == SwipeToDismissBoxValue.EndToStart) currentOnRemove()
    }
    SwipeToDismissBox(
        state = state,
        modifier = modifier,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = Spacing.md)
                    .clip(TMShapes.card)
                    .background(colors.error.copy(alpha = 0.15f))
                    .padding(horizontal = Spacing.xl),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(Icons.Rounded.Delete, contentDescription = "Remove", tint = colors.error)
            }
        },
    ) {
        content()
    }
}
