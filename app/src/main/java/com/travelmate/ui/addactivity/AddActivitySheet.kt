package com.travelmate.ui.addactivity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.travelmate.core.Formatters
import com.travelmate.domain.model.Place
import com.travelmate.domain.model.PlaceType
import com.travelmate.ui.components.EmptyState
import com.travelmate.ui.components.GlassCard
import com.travelmate.ui.components.RemoteImage
import com.travelmate.ui.components.TMChip
import com.travelmate.ui.components.TMTextField
import com.travelmate.ui.components.label
import com.travelmate.ui.components.pluralLabel
import com.travelmate.ui.theme.Spacing
import com.travelmate.ui.theme.TMShapes
import com.travelmate.ui.theme.ThemePreviews
import com.travelmate.ui.theme.PreviewSurface
import com.travelmate.ui.theme.TravelMateTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddActivitySheet(
    tripId: String,
    day: Int,
    onDismiss: () -> Unit,
) {
    val viewModel: AddActivityViewModel = viewModel(
        key = "add-activity-$tripId-$day",
        factory = AddActivityViewModel.factory(tripId, day),
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.onEvent(AddActivityEvent.Opened)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = TravelMateTheme.colors.surface,
        shape = TMShapes.sheet,
    ) {
        AddActivityContent(state = state, onEvent = viewModel::onEvent)
    }
}

@Composable
fun AddActivityContent(
    state: AddActivityUiState,
    onEvent: (AddActivityEvent) -> Unit
) {
    val colors = TravelMateTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = Spacing.xl),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Row(
            Modifier.padding(horizontal = Spacing.gutter),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Add activity · Day ${state.day}",
                style = MaterialTheme.typography.headlineSmall,
                color = colors.textPrimary,
                modifier = Modifier.weight(1f),
            )

            if (state.addedCount > 0) {
                Text(
                    "${state.addedCount} added",
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.primary
                )
            }
        }

        TMTextField(
            value = state.query,
            onValueChange = {
                onEvent(AddActivityEvent.QueryChanged(it))
            },
            placeholder = "Search places",
            leadingIcon = Icons.Rounded.Search,
            modifier = Modifier.padding(horizontal = Spacing.gutter),
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = Spacing.gutter),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            item {
                TMChip(
                    label = "All",
                    selected = state.filter == null,
                    onClick = {
                        onEvent(AddActivityEvent.FilterChanged(null))
                    }
                )
            }

            items(PlaceType.entries) { type ->
                TMChip(
                    label = type.pluralLabel,
                    selected = state.filter == type,
                    onClick = {
                        onEvent(AddActivityEvent.FilterChanged(type))
                    },
                )
            }
        }

        state.message?.let {
            Text(
                it,
                style = MaterialTheme.typography.labelMedium,
                color = colors.error,
                modifier = Modifier.padding(horizontal = Spacing.gutter),
            )
        }

        Box(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 160.dp, max = 460.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                state.loading -> CircularProgressIndicator(color = colors.primary)

                state.error != null -> EmptyState(
                    icon = Icons.Rounded.CloudOff,
                    title = "Could not load places",
                    message = state.error
                )

                state.candidates.isEmpty() -> EmptyState(
                    icon = Icons.Rounded.SearchOff,
                    title = if (state.query.isBlank() && state.filter == null)
                        "Every place is already in your trip"
                    else
                        "No places match",
                    message = if (state.query.isBlank() && state.filter == null)
                        null
                    else
                        "Try another name or type.",
                )

                else -> LazyColumn(
                    contentPadding = PaddingValues(horizontal = Spacing.gutter),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    items(state.candidates, key = { it.id }) { place ->
                        CandidateRow(
                            place = place,
                            onAdd = {
                                onEvent(AddActivityEvent.Add(place))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CandidateRow(
    place: Place,
    onAdd: () -> Unit
) {
    val colors = TravelMateTheme.colors

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(Spacing.sm)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            RemoteImage(
                url = place.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(56.dp)
                    .clip(TMShapes.chip)
            )

            Column(Modifier.weight(1f)) {
                Text(
                    place.name,
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    "${place.type.label} · ${Formatters.priceOrFree(place.priceSar)} · ${Formatters.duration(place.durationMin)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textSecondary,
                )
            }

            IconButton(onClick = onAdd) {
                Icon(
                    Icons.Rounded.AddCircle,
                    contentDescription = "Add ${place.name}",
                    tint = colors.primary
                )
            }
        }
    }
}

@ThemePreviews
@Composable
private fun AddActivityEmptyPreview() = PreviewSurface {
    AddActivityContent(
        state = AddActivityUiState(loading = false, day = 2),
        onEvent = {}
    )
}

@ThemePreviews
@Composable
private fun AddActivityDayFullPreview() = PreviewSurface {
    AddActivityContent(
        state = AddActivityUiState(
            loading = false,
            day = 1,
            addedCount = 2,
            message = "Day 1 is full: SAMPLE Place would end after 22:00."
        ),
        onEvent = {},
    )
}

@ThemePreviews
@Composable
private fun AddActivityLoadingPreview() = PreviewSurface {
    AddActivityContent(
        state = AddActivityUiState(loading = true),
        onEvent = {}
    )
}
