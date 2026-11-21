package com.travelmate.ui.addactivity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.travelmate.core.ErrorMessages
import com.travelmate.data.repository.CatalogRepository
import com.travelmate.data.repository.TripRepository
import com.travelmate.di.appContainer
import com.travelmate.domain.model.Destination
import com.travelmate.domain.model.Place
import com.travelmate.domain.model.PlaceType
import com.travelmate.domain.model.Trip
import com.travelmate.domain.usecase.EditItineraryUseCase
import com.travelmate.domain.usecase.ItineraryEdit
import com.travelmate.domain.usecase.PlanningRules
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class AddActivityUiState(
    val loading: Boolean = true,
    val day: Int = 1,
    val query: String = "",
    val filter: PlaceType? = null,
    val candidates: List<Place> = emptyList(),
    val addedCount: Int = 0,
    val message: String? = null,
    val error: String? = null,
)

sealed interface AddActivityEvent {
    data object Opened : AddActivityEvent
    data class QueryChanged(val query: String) : AddActivityEvent
    data class FilterChanged(val filter: PlaceType?) : AddActivityEvent
    data class Add(val place: Place) : AddActivityEvent
}

private data class AddData(
    val trip: Trip,
    val destination: Destination?,
    val places: List<Place>
)

@OptIn(ExperimentalCoroutinesApi::class)
class AddActivityViewModel(
    private val tripId: String,
    day: Int,
    private val tripRepository: TripRepository,
    private val catalogRepository: CatalogRepository,
    private val editItinerary: EditItineraryUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(AddActivityUiState(day = day))
    val state: StateFlow<AddActivityUiState> = _state.asStateFlow()

    private var data: AddData? = null

    init {
        viewModelScope.launch {
            tripRepository.trip(tripId)
                .filterNotNull()
                .flatMapLatest { trip ->
                    combine(
                        catalogRepository.destination(trip.destinationId),
                        catalogRepository.places(trip.destinationId),
                    ) { destination, places ->
                        AddData(trip, destination, places)
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
                .collect { newData ->
                    data = newData
                    refresh()
                }
        }
    }

    fun onEvent(event: AddActivityEvent) {
        when (event) {
            AddActivityEvent.Opened -> {
                _state.update {
                    it.copy(
                        addedCount = 0,
                        message = null,
                        query = "",
                        filter = null
                    )
                }
                refresh()
            }

            is AddActivityEvent.QueryChanged -> {
                _state.update { it.copy(query = event.query) }
                refresh()
            }

            is AddActivityEvent.FilterChanged -> {
                _state.update { it.copy(filter = event.filter) }
                refresh()
            }

            is AddActivityEvent.Add -> add(event.place)
        }
    }

    private fun add(place: Place) {
        val current = data ?: return
        val destination = current.destination ?: return
        val day = _state.value.day

        val durations = current.places.associate {
            it.id to it.durationMin
        }

        when (
            val result = editItinerary.add(
                current.trip,
                day,
                place,
                UUID.randomUUID().toString(),
                destination,
                durations
            )
        ) {
            ItineraryEdit.DayFull -> {
                _state.update {
                    it.copy(
                        message = "Day $day is full: ${place.name} would end after 22:00."
                    )
                }
            }

            is ItineraryEdit.Updated -> {
                data = current.copy(
                    trip = current.trip.copy(
                        items = result.items,
                        totalCostSar = result.cost.totalSar
                    )
                )

                _state.update {
                    it.copy(
                        addedCount = it.addedCount + 1,
                        message = null
                    )
                }

                refresh()

                viewModelScope.launch {
                    tripRepository.replaceItems(
                        tripId,
                        result.items,
                        result.cost
                    ).onFailure { t ->
                        _state.update {
                            it.copy(
                                message = ErrorMessages.userMessage(t)
                            )
                        }
                    }
                }
            }
        }
    }

    private fun refresh() {
        val current = data ?: return
        val inTrip = current.trip.items.map {
            it.placeId
        }.toSet()

        _state.update { s ->
            val q = s.query.trim()

            s.copy(
                loading = false,
                error = null,
                candidates = current.places
                    .asSequence()
                    .filter { it.id !in inTrip }
                    .filter {
                        s.filter == null || it.type == s.filter
                    }
                    .filter {
                        q.isEmpty() || it.name.contains(q, ignoreCase = true)
                    }
                    .sortedWith(PlanningRules.placeOrder)
                    .toList(),
            )
        }
    }

    companion object {
        fun factory(tripId: String, day: Int) = viewModelFactory {
            initializer {
                val c = appContainer()

                AddActivityViewModel(
                    tripId,
                    day,
                    c.tripRepository,
                    c.catalogRepository,
                    c.editItinerary
                )
            }
        }
    }
}