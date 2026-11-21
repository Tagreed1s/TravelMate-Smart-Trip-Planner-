package com.travelmate.fakes

import com.travelmate.data.repository.AuthRepository
import com.travelmate.data.repository.CatalogRepository
import com.travelmate.data.repository.TripRepository
import com.travelmate.domain.model.Destination
import com.travelmate.domain.model.Expense
import com.travelmate.domain.model.ItineraryItem
import com.travelmate.domain.model.Place
import com.travelmate.domain.model.Trip
import com.travelmate.domain.model.TripCost
import com.travelmate.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** In-memory fakes for ViewModel tests (next phase). */
class FakeAuthRepository(initialUser: User? = User("sample-uid", "SAMPLE User", "sample@example.com")) : AuthRepository {
    private val _currentUser = MutableStateFlow(initialUser)
    override val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    override suspend fun signIn(email: String, password: String): Result<Unit> {
        _currentUser.value = User("sample-uid", email.substringBefore("@"), email)
        return Result.success(Unit)
    }

    override suspend fun signUp(name: String, email: String, password: String): Result<Unit> {
        _currentUser.value = User("sample-uid", name, email)
        return Result.success(Unit)
    }

    override suspend fun sendPasswordReset(email: String): Result<Unit> = Result.success(Unit)

    override fun signOut() {
        _currentUser.value = null
    }
}

class FakeCatalogRepository(
    destinations: List<Destination> = listOf(SampleData.destinationA, SampleData.destinationB),
    places: Map<String, List<Place>> = destinations.associate { it.id to SampleData.places(it.id) },
) : CatalogRepository {
    val destinationsFlow = MutableStateFlow(destinations)
    val placesFlow = MutableStateFlow(places)

    override fun destinations(): Flow<List<Destination>> =
        destinationsFlow.map { list -> list.sortedWith(compareBy<Destination> { it.sortOrder }.thenBy { it.name }) }

    override fun destination(id: String): Flow<Destination?> =
        destinationsFlow.map { list -> list.firstOrNull { it.id == id } }

    override fun places(destinationId: String): Flow<List<Place>> =
        placesFlow.map { it[destinationId].orEmpty() }
}

class FakeTripRepository : TripRepository {
    val tripsFlow = MutableStateFlow<Map<String, Trip>>(emptyMap())
    private var nextId = 1

    override fun trips(): Flow<List<Trip>> = tripsFlow.map { it.values.sortedByDescending { trip -> trip.updatedAt } }

    override fun trip(tripId: String): Flow<Trip?> = tripsFlow.map { it[tripId] }

    override suspend fun createTrip(trip: Trip): Result<String> {
        val id = "sample-trip-${nextId++}"
        tripsFlow.update { it + (id to trip.copy(id = id)) }
        return Result.success(id)
    }

    override suspend fun replaceItems(tripId: String, items: List<ItineraryItem>, cost: TripCost): Result<Unit> {
        val trip = tripsFlow.value[tripId] ?: return Result.failure(IllegalStateException("No trip $tripId"))
        tripsFlow.update {
            it + (tripId to trip.copy(
                items = items,
                totalCostSar = cost.totalSar,
                placesCount = items.map { item -> item.placeId }.distinct().size,
            ))
        }
        return Result.success(Unit)
    }

    override suspend fun saveExpense(tripId: String, expense: Expense, totalCostSar: Int): Result<String> {
        val trip = tripsFlow.value[tripId] ?: return Result.failure(IllegalStateException("No trip $tripId"))
        val id = expense.id.ifBlank { "sample-expense-${nextId++}" }
        val expenses = trip.expenses.filter { it.id != id } + expense.copy(id = id)
        tripsFlow.update { it + (tripId to trip.copy(expenses = expenses, totalCostSar = totalCostSar)) }
        return Result.success(id)
    }

    override suspend fun deleteExpense(tripId: String, expenseId: String, totalCostSar: Int): Result<Unit> {
        val trip = tripsFlow.value[tripId] ?: return Result.failure(IllegalStateException("No trip $tripId"))
        tripsFlow.update {
            it + (tripId to trip.copy(expenses = trip.expenses.filter { e -> e.id != expenseId }, totalCostSar = totalCostSar))
        }
        return Result.success(Unit)
    }

    override suspend fun renameTrip(tripId: String, name: String): Result<Unit> {
        val trip = tripsFlow.value[tripId] ?: return Result.failure(IllegalStateException("No trip $tripId"))
        tripsFlow.update { it + (tripId to trip.copy(name = name.trim())) }
        return Result.success(Unit)
    }

    override suspend fun deleteTrip(tripId: String): Result<Unit> {
        tripsFlow.update { it - tripId }
        return Result.success(Unit)
    }
}
