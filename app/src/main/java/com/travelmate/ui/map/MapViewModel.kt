package com.travelmate.ui.map

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.travelmate.core.ErrorMessages
import com.travelmate.data.repository.CatalogRepository
import com.travelmate.data.repository.TripRepository
import com.travelmate.di.appContainer
import com.travelmate.domain.model.Destination
import com.travelmate.domain.model.Place
import com.travelmate.domain.model.Trip
import com.travelmate.domain.util.Distance
import com.travelmate.ui.navigation.NavArgs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MapStop(
    val number: Int,
    val place: Place,
    val startMin: Int
)

data class MapLeg(
    val from: Int,
    val to: Int,
    val km: Double
)

data class MapUiState(
    val loading: Boolean = true,
    val notFound: Boolean = false,
    val error: String? = null,
    val trip: Trip? = null,
    val destination: Destination? = null,
    val placesById: Map<String, Place> = emptyMap(),
    val selectedDay: Int = 1,
) {

    val stops: List<MapStop>
        get() = trip?.itemsForDay(selectedDay).orEmpty()
            .mapNotNull { item ->
                placesById[item.placeId]?.let { item to it }
            }
            .mapIndexed { index, (item, place) ->
                MapStop(
                    number = index + 1,
                    place = place,
                    startMin = item.startMin
                )
            }

    val legs: List<MapLeg>
        get() = stops.zipWithNext { a, b ->
            MapLeg(
                a.number,
                b.number,
                Distance.km(
                    a.place.lat,
                    a.place.lng,
                    b.place.lat,
                    b.place.lng
                )
            )
        }

    val totalKm: Double
        get() = legs.sumOf { it.km }
}

sealed interface MapEvent {
    data class DaySelected(val day: Int) : MapEvent
}

private data class MapData(
    val trip: Trip?,
    val destination: Destination?,
    val places: List<Place>
)

@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModel(
    savedStateHandle: SavedStateHandle,
    tripRepository: TripRepository,
    catalogRepository: CatalogRepository,
) : ViewModel() {

    private val tripId: String =
        checkNotNull(savedStateHandle[NavArgs.TRIP_ID])

    private val _state = MutableStateFlow(MapUiState())
    val state: StateFlow<MapUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            tripRepository.trip(tripId)
                .flatMapLatest { trip ->
                    if (trip == null) {
                        flowOf(
                            MapData(
                                null,
                                null,
                                emptyList()
                            )
                        )
                    } else {
                        combine(
                            catalogRepository.destination(trip.destinationId)
                                .catch { emit(null) },

                            catalogRepository.places(trip.destinationId)
                                .catch { emit(emptyList()) },

                            ) { destination, places ->
                            MapData(trip, destination, places)
                        }
                    }
                }
                .catch { t ->
                    _state.update {
                        it.copy(
                            loading = false,
                            error = ErrorMessages.userMessage(t)
                        )
                    }
                }
                .collect { data ->
                    _state.update {
                        it.copy(
                            loading = false,
                            error = null,
                            notFound = data.trip == null,
                            trip = data.trip,
                            destination = data.destination,
                            placesById = data.places.associateBy {
                                    place -> place.id
                            },
                            selectedDay = it.selectedDay.coerceIn(
                                1,
                                maxOf(data.trip?.days ?: 1, 1)
                            ),
                        )
                    }
                }
        }
    }

    fun onEvent(event: MapEvent) {
        when (event) {
            is MapEvent.DaySelected -> {
                _state.update {
                    it.copy(selectedDay = event.day)
                }
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val c = appContainer()

                MapViewModel(
                    createSavedStateHandle(),
                    c.tripRepository,
                    c.catalogRepository
                )
            }
        }
    }
}