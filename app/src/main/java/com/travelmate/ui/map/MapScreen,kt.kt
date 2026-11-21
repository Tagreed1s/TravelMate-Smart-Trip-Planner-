package com.travelmate.ui.map

import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.travelmate.core.Formatters
import com.travelmate.domain.model.Trip
import com.travelmate.ui.components.DayPills
import com.travelmate.ui.components.EmptyState
import com.travelmate.ui.components.GlassCard
import com.travelmate.ui.components.TMTopBar
import com.travelmate.ui.theme.AmbientBackground
import com.travelmate.ui.theme.NumberStyle
import com.travelmate.ui.theme.Spacing
import com.travelmate.ui.theme.TMShapes
import com.travelmate.ui.theme.ThemePreviews
import com.travelmate.ui.theme.TravelMateTheme
import java.util.Locale

@Composable
fun MapScreen(
    onBack: () -> Unit,
    viewModel: MapViewModel = viewModel(factory = MapViewModel.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    MapContent(
        state = state,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        mapAvailable = rememberMapsKeyAvailable()
    )
}

@Composable
fun MapContent(
    state: MapUiState,
    onEvent: (MapEvent) -> Unit,
    onBack: () -> Unit,
    mapAvailable: Boolean,
) {
    val trip = state.trip

    AmbientBackground {
        Column(Modifier.fillMaxSize()) {

            TMTopBar(
                title = "Map",
                subtitle = trip?.name,
                onBack = onBack
            )

            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when {
                    state.loading -> CenteredMessage {
                        CircularProgressIndicator(
                            color = TravelMateTheme.colors.primary
                        )
                    }

                    state.error != null -> CenteredMessage {
                        EmptyState(
                            icon = Icons.Rounded.CloudOff,
                            title = "Could not load this trip",
                            message = state.error
                        )
                    }

                    state.notFound || trip == null -> CenteredMessage {
                        EmptyState(
                            icon = Icons.Rounded.SearchOff,
                            title = "Trip not found",
                            actionLabel = "Go back",
                            onAction = onBack
                        )
                    }

                    else -> MapBody(
                        state = state,
                        trip = trip,
                        onEvent = onEvent,
                        mapAvailable = mapAvailable
                    )
                }
            }
        }
    }
}

@Composable
private fun MapBody(
    state: MapUiState,
    trip: Trip,
    onEvent: (MapEvent) -> Unit,
    mapAvailable: Boolean
) {
    val stops = state.stops

    Column(Modifier.fillMaxSize()) {

        DayPills(
            days = trip.days,
            startDate = trip.startDate,
            selectedDay = state.selectedDay,
            onSelect = {
                onEvent(MapEvent.DaySelected(it))
            },
        )

        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(Spacing.gutter)
                .clip(TMShapes.card),
            contentAlignment = Alignment.Center,
        ) {
            when {
                !mapAvailable -> EmptyState(
                    icon = Icons.Rounded.Map,
                    title = "Map unavailable",
                    message = "Add MAPS_API_KEY to local.properties to show the map. Distances are listed below.",
                )

                else -> TripGoogleMap(
                    stops = stops,
                    centerLat = state.destination?.lat
                        ?: stops.firstOrNull()?.place?.lat
                        ?: 0.0,
                    centerLng = state.destination?.lng
                        ?: stops.firstOrNull()?.place?.lng
                        ?: 0.0,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        DistanceCard(
            state = state,
            modifier = Modifier
                .navigationBarsPadding()
                .padding(
                    horizontal = Spacing.gutter,
                    vertical = Spacing.sm
                )
        )
    }
}

@Composable
private fun TripGoogleMap(
    stops: List<MapStop>,
    centerLat: Double,
    centerLng: Double,
    modifier: Modifier = Modifier
) {
    val points = remember(stops) {
        stops.map {
            LatLng(it.place.lat, it.place.lng)
        }
    }

    val center = LatLng(centerLat, centerLng)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            points.firstOrNull() ?: center,
            12f
        )
    }

    var mapLoaded by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(mapLoaded, points) {
        if (!mapLoaded) return@LaunchedEffect

        val update = when {
            points.size >= 2 -> {
                val bounds = LatLngBounds.builder()
                    .apply {
                        points.forEach {
                            include(it)
                        }
                    }
                    .build()

                CameraUpdateFactory.newLatLngBounds(
                    bounds,
                    MAP_PADDING_PX
                )
            }

            points.size == 1 -> {
                CameraUpdateFactory.newLatLngZoom(
                    points.first(),
                    13f
                )
            }

            else -> {
                CameraUpdateFactory.newLatLngZoom(
                    center,
                    11f
                )
            }
        }

        cameraPositionState.animate(update, 600)
    }

    val lineColor = TravelMateTheme.colors.primary

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        uiSettings = MapUiSettings(
            zoomControlsEnabled = false,
            mapToolbarEnabled = false
        ),
        onMapLoaded = {
            mapLoaded = true
        },
    ) {
        stops.forEachIndexed { index, stop ->
            Marker(
                state = remember(stop.place.id) {
                    MarkerState(position = points[index])
                },
                title = "${stop.number}. ${stop.place.name}",
                snippet = Formatters.time(stop.startMin),
            )
        }

        if (points.size >= 2) {
            Polyline(
                points = points,
                color = lineColor,
                width = 10f
            )
        }
    }
}

@Composable
private fun DistanceCard(
    state: MapUiState,
    modifier: Modifier = Modifier
) {
    val colors = TravelMateTheme.colors
    val stops = state.stops

    GlassCard(modifier.fillMaxWidth()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            Text(
                "Distances · Day ${state.selectedDay}",
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary
            )

            if (stops.size < 2) {
                Text(
                    if (stops.isEmpty())
                        "Nothing planned for this day."
                    else
                        "Add another place to see distances.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                )
            } else {

                stops.forEach { stop ->
                    Text(
                        "${stop.number}. ${stop.place.name} · ${Formatters.time(stop.startMin)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.textSecondary,
                    )
                }

                state.legs.forEach { leg ->
                    Row {
                        Text(
                            "${leg.from} to ${leg.to}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textPrimary,
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            km(leg.km),
                            style = NumberStyle,
                            color = colors.textPrimary
                        )
                    }
                }

                Row {
                    Text(
                        "Day total",
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.primary,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        km(state.totalKm),
                        style = NumberStyle,
                        color = colors.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun CenteredMessage(
    content: @Composable () -> Unit
) {
    Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun rememberMapsKeyAvailable(): Boolean {
    val context = LocalContext.current
    val inPreview = LocalInspectionMode.current

    return remember {
        if (inPreview) return@remember false

        runCatching {
            @Suppress("DEPRECATION")
            val info = context.packageManager.getApplicationInfo(
                context.packageName,
                PackageManager.GET_META_DATA
            )

            !info.metaData
                ?.getString(MAPS_KEY_META)
                .isNullOrBlank()

        }.getOrDefault(false)
    }
}

private fun km(value: Double): String =
    "%.1f km".format(Locale.US, value)

private const val MAPS_KEY_META =
    "com.google.android.geo.API_KEY"

private const val MAP_PADDING_PX = 120

@ThemePreviews
@Composable
private fun MapLoadingPreview() = TravelMateTheme {
    MapContent(
        state = MapUiState(loading = true),
        onEvent = {},
        onBack = {},
        mapAvailable = false
    )
}

@ThemePreviews
@Composable
private fun MapNotFoundPreview() = TravelMateTheme {
    MapContent(
        state = MapUiState(
            loading = false,
            notFound = true
        ),
        onEvent = {},
        onBack = {},
        mapAvailable = false
    )
}