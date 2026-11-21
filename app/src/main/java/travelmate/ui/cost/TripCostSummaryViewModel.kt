package com.travelmate.ui.screens.cost

import androidx.lifecycle.SavedStateHandle
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
import com.travelmate.domain.model.Expense
import com.travelmate.domain.model.ExpenseCategory
import com.travelmate.domain.model.Trip
import com.travelmate.domain.model.TripCost
import com.travelmate.domain.usecase.CalculateTripCostUseCase
import com.travelmate.ui.components.label
import com.travelmate.ui.navigation.NavArgs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** One row of the breakdown: what the app calculated plus what the user added in Edit Expenses. */
data class CostLine(val category: ExpenseCategory, val calculatedSar: Int, val addedSar: Int) {
    val totalSar: Int get() = calculatedSar + addedSar
}

enum class ExpenseField { TITLE, AMOUNT }

/** The add / edit form of the Edit Expenses sheet. A blank [id] means a new expense. */
data class ExpenseForm(
    val id: String = "",
    val category: ExpenseCategory = ExpenseCategory.FLIGHTS,
    val title: String = "",
    val amountText: String = "",
    val createdAt: Long = 0,
    val errors: Map<ExpenseField, String> = emptyMap(),
)

data class TripCostSummaryUiState(
    val loading: Boolean = true,
    val notFound: Boolean = false,
    val error: String? = null,
    val trip: Trip? = null,
    val destination: Destination? = null,
    val cost: TripCost? = null,
    val lines: List<CostLine> = emptyList(),
    /** "SAR 900 x 2 nights x 1 room". */
    val stayNote: String? = null,
    val showEditExpenses: Boolean = false,
    val form: ExpenseForm = ExpenseForm(),
    val saving: Boolean = false,
    val message: String? = null,
)

sealed interface TripCostSummaryEvent {
    data object EditExpensesClicked : TripCostSummaryEvent
    data object EditExpensesDismissed : TripCostSummaryEvent
    data class FormCategoryChanged(val category: ExpenseCategory) : TripCostSummaryEvent
    data class FormTitleChanged(val title: String) : TripCostSummaryEvent
    data class FormAmountChanged(val text: String) : TripCostSummaryEvent
    data object SaveExpense : TripCostSummaryEvent
    data class EditExpense(val expense: Expense) : TripCostSummaryEvent
    data object CancelEdit : TripCostSummaryEvent
    data class DeleteExpense(val expenseId: String) : TripCostSummaryEvent
    data object MessageShown : TripCostSummaryEvent
}

private data class CostData(val trip: Trip?, val destination: Destination?)

/** Route argument: tripId (trip_cost_summary/{tripId}). Also drives the Edit Expenses sheet. */
@OptIn(ExperimentalCoroutinesApi::class)
class TripCostSummaryViewModel(
    savedStateHandle: SavedStateHandle,
    private val tripRepository: TripRepository,
    catalogRepository: CatalogRepository,
    private val calculateTripCost: CalculateTripCostUseCase,
) : ViewModel() {

    private val tripId: String = checkNotNull(savedStateHandle[NavArgs.TRIP_ID])

    private val _state = MutableStateFlow(TripCostSummaryUiState())
    val state: StateFlow<TripCostSummaryUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            tripRepository.trip(tripId)
                .flatMapLatest { trip ->
                    if (trip == null) {
                        flowOf(CostData(null, null))
                    } else {
                        catalogRepository.destination(trip.destinationId)
                            .catch { emit(null) }
                            .map { destination -> CostData(trip, destination) }
                    }
                }
                .catch { t -> _state.update { it.copy(loading = false, error = ErrorMessages.userMessage(t)) } }
                .collect { data -> _state.update { it.withData(data.trip, data.destination) } }
        }
    }

    fun onEvent(event: TripCostSummaryEvent) {
        when (event) {
            TripCostSummaryEvent.EditExpensesClicked -> _state.update { it.copy(showEditExpenses = true, form = ExpenseForm()) }
            TripCostSummaryEvent.EditExpensesDismissed -> _state.update { it.copy(showEditExpenses = false) }
            is TripCostSummaryEvent.FormCategoryChanged -> updateForm { it.copy(category = event.category) }
            is TripCostSummaryEvent.FormTitleChanged -> updateForm { it.copy(title = event.title.take(MAX_TITLE)) }
            is TripCostSummaryEvent.FormAmountChanged -> updateForm {
                it.copy(amountText = event.text.filter(Char::isDigit).take(MAX_AMOUNT_DIGITS))
            }
            TripCostSummaryEvent.SaveExpense -> saveExpense()
            is TripCostSummaryEvent.EditExpense -> _state.update {
                it.copy(
                    form = ExpenseForm(
                        id = event.expense.id,
                        category = event.expense.category,
                        title = event.expense.title,
                        amountText = event.expense.amountSar.toString(),
                        createdAt = event.expense.createdAt,
                    ),
                )
            }
            TripCostSummaryEvent.CancelEdit -> _state.update { it.copy(form = ExpenseForm()) }
            is TripCostSummaryEvent.DeleteExpense -> deleteExpense(event.expenseId)
            TripCostSummaryEvent.MessageShown -> _state.update { it.copy(message = null) }
        }
    }

    private fun updateForm(transform: (ExpenseForm) -> ExpenseForm) {
        _state.update { it.copy(form = transform(it.form).copy(errors = emptyMap())) }
    }

    private fun saveExpense() {
        val current = _state.value
        val trip = current.trip ?: return
        val destination = current.destination ?: return
        val form = current.form
        val amount = form.amountText.toIntOrNull()
        val errors = buildMap<ExpenseField, String> {
            if (amount == null || amount <= 0) put(ExpenseField.AMOUNT, "Enter an amount in SAR")
        }
        if (errors.isNotEmpty()) {
            _state.update { it.copy(form = form.copy(errors = errors)) }
            return
        }
        val expense = Expense(
            id = form.id,
            category = form.category,
            // A blank title falls back to the category name, for example "Flights".
            title = form.title.trim().ifEmpty { form.category.label },
            amountSar = requireNotNull(amount),
            createdAt = form.createdAt,
        )
        val newExpenses = trip.expenses.filter { it.id != expense.id || expense.id.isEmpty() } + expense
        val total = calculateTripCost(destination, trip.days, trip.travelers, trip.items, newExpenses).totalSar
        _state.update { it.copy(saving = true) }
        viewModelScope.launch {
            tripRepository.saveExpense(trip.id, expense, total)
                .onSuccess { _state.update { it.copy(saving = false, form = ExpenseForm()) } }
                .onFailure { t -> _state.update { it.copy(saving = false, message = ErrorMessages.userMessage(t)) } }
        }
    }

    private fun deleteExpense(expenseId: String) {
        val current = _state.value
        val trip = current.trip ?: return
        val destination = current.destination ?: return
        val newExpenses = trip.expenses.filter { it.id != expenseId }
        val total = calculateTripCost(destination, trip.days, trip.travelers, trip.items, newExpenses).totalSar
        if (current.form.id == expenseId) _state.update { it.copy(form = ExpenseForm()) }
        viewModelScope.launch {
            tripRepository.deleteExpense(trip.id, expenseId, total)
                .onFailure { t -> _state.update { it.copy(message = ErrorMessages.userMessage(t)) } }
        }
    }

    private fun TripCostSummaryUiState.withData(trip: Trip?, destination: Destination?): TripCostSummaryUiState {
        if (trip == null || destination == null) {
            return copy(loading = false, error = null, notFound = trip == null, trip = trip, destination = destination)
        }
        val cost = calculateTripCost(destination, trip.days, trip.travelers, trip.items, trip.expenses)
        val added = trip.expenses.groupBy { it.category }.mapValues { (_, list) -> list.sumOf { it.amountSar } }
        fun line(category: ExpenseCategory, calculated: Int) = CostLine(category, calculated, added[category] ?: 0)
        val lines = listOf(
            line(ExpenseCategory.FLIGHTS, 0),
            line(ExpenseCategory.STAY, cost.accommodationSar),
            line(ExpenseCategory.FOOD, cost.foodSar),
            line(ExpenseCategory.TRANSPORT, cost.transportSar),
            line(ExpenseCategory.ACTIVITIES, cost.activitiesSar),
            line(ExpenseCategory.OTHER, 0),
        ).filter { it.category != ExpenseCategory.OTHER || it.totalSar > 0 }
        val nights = calculateTripCost.nights(trip.days)
        val rooms = calculateTripCost.rooms(trip.travelers)
        return copy(
            loading = false,
            error = null,
            notFound = false,
            trip = trip,
            destination = destination,
            cost = cost,
            lines = lines,
            stayNote = "${Formatters.money(destination.accommodationPerNightSar)} x " +
                    "$nights night${if (nights == 1) "" else "s"} x $rooms room${if (rooms == 1) "" else "s"}",
        )
    }

    companion object {
        private const val MAX_TITLE = 40
        private const val MAX_AMOUNT_DIGITS = 7

        val Factory = viewModelFactory {
            initializer {
                val c = appContainer()
                TripCostSummaryViewModel(createSavedStateHandle(), c.tripRepository, c.catalogRepository, c.calculateTripCost)
            }
        }
    }
}

