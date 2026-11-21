package com.travelmate.ui.screens.destination

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.travelmate.core.Formatters
import com.travelmate.domain.model.Destination
import com.travelmate.domain.model.Place
import com.travelmate.domain.model.PlaceType
import com.travelmate.ui.components.FavoriteSlot
import com.travelmate.ui.components.GlassCard
import com.travelmate.ui.components.GlassIconButton
import com.travelmate.ui.components.ImageScrim
import com.travelmate.ui.components.PlaceCard
import com.travelmate.ui.components.RemoteImage
import com.travelmate.ui.components.TMChip
import com.travelmate.ui.theme.Spacing
import com.travelmate.ui.theme.TravelMateTheme

/**320 dp image pager with scrim, page dots, a glass back button and the save (heart) button. */
@Composable
fun DestinationHero(
    destination: Destination,
    onBack: () -> Unit,
    isFavorite: Boolean? = null,
    onToggleFavorite: (() -> Unit)? = null,
) {
    val colors = TravelMateTheme.colors
    val images = destination.imageUrls.ifEmpty { listOf(destination.heroImageUrl) }
    val pagerState = rememberPagerState { images.size }
    Box(Modifier.fillMaxWidth().height(320.dp)) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            RemoteImage(url = images[page], contentDescription = destination.name, modifier = Modifier.fillMaxSize())
        }
        ImageScrim(Modifier.fillMaxSize())
        Row(
            modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(Spacing.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            GlassIconButton(icon = Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", onClick = onBack)
            FavoriteSlot(isFavorite = isFavorite, onToggleFavorite = onToggleFavorite)
        }
        if (images.size > 1) {
            Row(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = Spacing.md),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                repeat(images.size) { index ->
                    val selected = index == pagerState.currentPage
                    Box(
                        Modifier
                            .size(if (selected) 8.dp else 6.dp)
                            .background(if (selected) colors.primary else colors.textSecondary, CircleShape),
                    )
                }
            }
        }
    }
}

/** Three small cards: best time, daily spend, weather. */
@Composable
fun StatRow(destination: Destination, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        StatCard("Best time", destination.bestTimeToVisit, Modifier.weight(1f))
        StatCard(
            "Daily spend",
            "${Formatters.money(destination.dailySpendMinSar)} - ${destination.dailySpendMaxSar}",
            Modifier.weight(1f),
        )
        StatCard("Weather", destination.weatherRange, Modifier.weight(1f))
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    val colors = TravelMateTheme.colors
    GlassCard(modifier = modifier, contentPadding = PaddingValues(Spacing.md)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
        Text(value, style = MaterialTheme.typography.labelLarge, color = colors.textPrimary, maxLines = 3, overflow = TextOverflow.Ellipsis)
    }
}

/** Summary plus description; the description collapses to 3 lines with More / Less. */
@Composable
fun AboutSection(
    destination: Destination,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TravelMateTheme.colors
    var overflows by remember { mutableStateOf(false) }
    Column(modifier.animateContentSize(), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        SectionTitle("About")
        Text(destination.summary, style = MaterialTheme.typography.bodyLarge, color = colors.textPrimary)
        Text(
            text = destination.description,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
            maxLines = if (expanded) Int.MAX_VALUE else 3,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { if (!expanded) overflows = it.hasVisualOverflow },
        )
        if (expanded || overflows) {
            TextButton(onClick = onToggle, contentPadding = PaddingValues(0.dp)) {
                Text(if (expanded) "Less" else "More", color = colors.secondary)
            }
        }
    }
}

/** Filter chips and a row of place cards. RESTAURANT is labelled "Food" here. */
@Composable
fun HighlightsSection(
    places: List<Place>,
    filter: PlaceType?,
    onFilterChange: (PlaceType?) -> Unit,
    onPlaceClick: (Place) -> Unit,
) {
    val filters = listOf(
        null to "All",
        PlaceType.ATTRACTION to "Attractions",
        PlaceType.RESTAURANT to "Food",
        PlaceType.ACTIVITY to "Activities",
        PlaceType.EVENT to "Events",
    )
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        SectionTitle("Highlights", Modifier.padding(horizontal = Spacing.gutter))
        LazyRow(
            contentPadding = PaddingValues(horizontal = Spacing.gutter),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            items(filters) { (type, label) ->
                TMChip(label = label, selected = filter == type, onClick = { onFilterChange(type) })
            }
        }
        if (places.isEmpty()) {
            Text(
                "Nothing in this category yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = TravelMateTheme.colors.textSecondary,
                modifier = Modifier.padding(horizontal = Spacing.gutter),
            )
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = Spacing.gutter),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                items(places, key = { it.id }) { place ->
                    PlaceCard(place = place, onClick = { onPlaceClick(place) })
                }
            }
        }
    }
}

/** "Tips (n)" card that expands to the list. */
@Composable
fun TipsSection(
    tips: List<String>,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TravelMateTheme.colors
    GlassCard(modifier = modifier.fillMaxWidth().animateContentSize(), onClick = onToggle) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Tips (${tips.size})", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary, modifier = Modifier.weight(1f))
            Icon(
                if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                contentDescription = if (expanded) "Hide tips" else "Show tips",
                tint = colors.textSecondary,
            )
        }
        AnimatedVisibility(visible = expanded) {
            Column(Modifier.padding(top = Spacing.md), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                tips.forEach { tip ->
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        Box(Modifier.padding(top = 8.dp).size(6.dp).background(colors.primary, CircleShape))
                        Text(tip, style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(text, style = MaterialTheme.typography.titleMedium, color = TravelMateTheme.colors.textPrimary, modifier = modifier)
}
