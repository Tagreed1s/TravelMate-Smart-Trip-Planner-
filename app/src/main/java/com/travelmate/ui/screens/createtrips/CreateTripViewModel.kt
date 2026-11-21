package com.travelmate.ui.screens.createtrips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.travelmate.core.ErrorMessages
import com.travelmate.core.Formatters
import com.travelmate.data.repository.CatalogRepository
import com.travelmate.data.repository.TripRepository
import com.travelmate.di.appContainer
import com.travelmate.domain.model.Destination
import com.travelmate.domain.model.Place
import com.travelmate.domain.model.PlaceType
import com.travelmate.domain.model.Trip
import com.travelmate.domain.model.TripRequest
import com.travelmate.domain.usecase.CalculateTripCostUseCase
import com.travelmate.domain.usecase.GenerateItineraryUseCase
import com.travelmate.domain.usecase.GenerationResult
import com.travelmate.domain.usecase.SuggestBudgetUseCase
import com.travelmate.domain.usecase.TripField
import com.travelmate.domain.usecase.ValidateTripInputUseCase
import com.travelmate.ui.navigation.NavArgs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

data class CreateTripUiState(
    val destinationId: String,
    val startDate: LocalDate,
    val destinations: List<Destination> = emptyList(),
    val destination: Destination? = null,
    val places: List<Place> = emptyList(),
    val placesLoaded: Boolean = false,
    val days: Int = 3,
    val travelers: Int = 2,
    val budgetText: String = "",
    /** True once the user typed a budget; from then on it no longer follows the suggestion. */
    val budgetEdited: Boolean = false,
    val interests: Set<PlaceType> = setOf(PlaceType.ATTRACTION, PlaceType.ACTIVITY),
    val suggestedBudgetSar: Int? = null,
    /** Stay + transport for the current days and travelers. */
    val fixedCostSar: Int? = null,
    val errors: Map<TripField, String> = emptyMap(),
    val submitted: Boolean = false,
    val generating: Boolean = false,
) {
    fun toRequest() = TripRequest(
        destinationId = destination?.id,
        startDate = startDate,
        days = days,
        travelers = travelers,
        budgetSar = budgetText.toIntOrNull(),
        interests = interests,
    )
}

sealed interface CreateTripEvent {
    data class DestinationChanged(val destinationId: String) : CreateTripEvent
    data class StartDateChanged(val date: LocalDate) : CreateTripEvent
    data class DaysChanged(val days: Int) : CreateTripEvent
    data class TravelersChanged(val travelers: Int) : CreateTripEvent
    data class BudgetChanged(val text: String) : CreateTripEvent
    data object UseSuggestion : CreateTripEvent
    data class InterestToggled(val type: PlaceType) : CreateTripEvent
    data object Generate : CreateTripEvent
}

sealed interface CreateTripEffect {
    data class TripCreated(val tripId: String) : CreateTripEffect
    data class ShowMessage(val text: String) : CreateTripEffect
}

@OptIn(ExperimentalCoroutinesApi::class)
class CreateTripViewModel(
    initialDestinationId: String,
    private val catalogRepository: CatalogRepository,
    private val tripRepository: TripRepository,
    private val validateTripInput: ValidateTripInputUseCase,
    private val generateItinerary: GenerateItineraryUseCase,
    private val suggestBudget: SuggestBudgetUseCase,
    private val calculateTripCost: CalculateTripCostUseCase,
    private val today: () -> LocalDate = { LocalDate.now() },
) : ViewModel() {

    private val selectedId = MutableStateFlow(initialDestinationId)

    private val _state = MutableStateFlow(
        CreateTripUiState(destinationId = initialDestinationId, startDate = today().plusDays(DEFAULT_START_OFFSET_DAYS)),
    )
    val state: StateFlow<CreateTripUiState> = _state.asStateFlow()

    private val _effects = Channel<CreateTripEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        viewModelScope.launch {
            catalogRepository.destinations()
                .catch { t -> _effects.send(CreateTripEffect.ShowMessage(ErrorMessages.userMessage(t))) }
                .collect { list ->
                    updateAndRecalculate {
                        it.copy(destinations = list, destination = list.firstOrNull { d -> d.id == it.destinationId })
                    }
                }
        }
        viewModelScope.launch {
            selectedId
                .flatMapLatest { id -> catalogRepository.places(id) }
                .catch { t -> _effects.send(CreateTripEffect.ShowMessage(ErrorMessages.userMessage(t))) }
                .collect { places -> _state.update { it.copy(places = places, placesLoaded = true) } }
        }
    }

    fun onEvent(event: CreateTripEvent) {
        when (event) {
            is CreateTripEvent.DestinationChanged -> {
                selectedId.value = event.destinationId
                updateAndRecalculate {
                    it.copy(
                        destinationId = event.destinationId,
                        destination = it.destinations.firstOrNull { d -> d.id == event.destinationId },
                        places = emptyList(),
                        placesLoaded = false,
                    )
                }
            }
            is CreateTripEvent.StartDateChanged -> updateAndRecalculate { it.copy(startDate = event.date) }
            is CreateTripEvent.DaysChanged -> updateAndRecalculate { it.copy(days = event.days) }
            is CreateTripEvent.TravelersChanged -> updateAndRecalculate { it.copy(travelers = event.travelers) }
            is CreateTripEvent.BudgetChanged -> updateAndRecalculate {
                it.copy(budgetText = event.text.filter(Char::isDigit).take(MAX_BUDGET_DIGITS), budgetEdited = true)
            }
            CreateTripEvent.UseSuggestion -> updateAndRecalculate {
                it.copy(budgetText = it.suggestedBudgetSar?.toString() ?: it.budgetText, budgetEdited = false)
            }
            is CreateTripEvent.InterestToggled -> updateAndRecalculate {
                val interests = if (event.type in it.interests) it.interests - event.type else it.interests + event.type
                it.copy(interests = interests)
            }
            CreateTripEvent.Generate -> generate()
        }
    }

    /** Applies a change, refreshes the suggestion and (after the first Generate) the field errors. */
    private fun updateAndRecalculate(transform: (CreateTripUiState) -> CreateTripUiState) {
        _state.update { old ->
            val changed = transform(old)
            val destination = changed.destination
            val withSuggestion = if (destination == null) {
                changed
            } else {
                val suggested = suggestBudget(destination, changed.days, changed.travelers)
                changed.copy(
                    suggestedBudgetSar = suggested,
                    fixedCostSar = calculateTripCost.fixedCost(destination, changed.days, changed.travelers),
                    budgetText = if (changed.budgetEdited) changed.budgetText else suggested.toString(),
                )
            }
            if (withSuggestion.submitted) {
                withSuggestion.copy(errors = validateTripInput(withSuggestion.toRequest(), today()))
            } else {
                withSuggestion
            }
        }
    }

    private fun generate() {
        val current = _state.value
        if (current.generating || !current.placesLoaded) return
        val request = current.toRequest()
        val errors = validateTripInput(request, today())
        _state.update { it.copy(submitted = true, errors = errors) }
        if (errors.isNotEmpty()) return
        val destination = current.destination ?: return

        when (val result = generateItinerary(destination, current.places, request) { UUID.randomUUID().toString() }) {
            is GenerationResult.BudgetTooLow -> _state.update {
                it.copy(
                    errors = it.errors + (TripField.BUDGET to
                            "Too low for stay and transport. Minimum ${Formatters.money(result.minimumSar)}."),
                )
            }
            is GenerationResult.Success -> save(destination, request, result)
        }
    }

    private fun save(destination: Destination, request: TripRequest, result: GenerationResult.Success) {
        val now = System.currentTimeMillis()
        val trip = Trip(
            id = "",
            destinationId = destination.id,
            destinationName = destination.name,
            heroImageUrl = destination.heroImageUrl,
            name = "${destination.name} Getaway",
            startDate = requireNotNull(request.startDate),
            days = request.days,
            travelers = request.travelers,
            budgetSar = requireNotNull(request.budgetSar),
            interests = request.interests,
            items = result.items,
            totalCostSar = result.cost.totalSar,
            placesCount = result.items.map { it.placeId }.distinct().size,
            createdAt = now,
            updatedAt = now,
        )
        _state.update { it.copy(generating = true) }
        viewModelScope.launch {
            val saved = tripRepository.createTrip(trip)
            _state.update { it.copy(generating = false) }
            saved
                .onSuccess { tripId -> _effects.send(CreateTripEffect.TripCreated(tripId)) }
                .onFailure { t -> _effects.send(CreateTripEffect.ShowMessage(ErrorMessages.userMessage(t))) }
        }
    }

    companion object {
        private const val DEFAULT_START_OFFSET_DAYS = 7L
        private const val MAX_BUDGET_DIGITS = 7

        /** Route argument: destinationId (create_trip/{destinationId}). */
        val Factory = viewModelFactory {
            initializer {
                val c = appContainer()
                CreateTripViewModel(
                    initialDestinationId = checkNotNull(createSavedStateHandle().get<String>(NavArgs.DESTINATION_ID)),
                    catalogRepository = c.catalogRepository,
                    tripRepository = c.tripRepository,
                    validateTripInput = c.validateTripInput,
                    generateItinerary = c.generateItinerary,
                    suggestBudget = c.suggestBudget,
                    calculateTripCost = c.calculateTripCost,
                )
            }
        }
    }
}
