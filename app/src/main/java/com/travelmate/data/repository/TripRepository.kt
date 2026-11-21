package com.travelmate.data.repository

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.android.gms.tasks.Task
import com.travelmate.data.dto.TripDto
import com.travelmate.data.dto.distinctPlaceCount
import com.travelmate.data.dto.readOrNull
import com.travelmate.data.dto.toDomain
import com.travelmate.data.dto.toWriteMap
import com.travelmate.data.firebase.FirebasePaths
import com.travelmate.data.firebase.valueFlow
import com.travelmate.domain.model.Expense
import com.travelmate.domain.model.ItineraryItem
import com.travelmate.domain.model.Trip
import com.travelmate.domain.model.TripCost
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

interface TripRepository {
    /** The signed-in user's trips, newest updatedAt first. */
    fun trips(): Flow<List<Trip>>
    fun trip(tripId: String): Flow<Trip?>

    /** Saves a new trip and returns its id. */
    suspend fun createTrip(trip: Trip): Result<String>

    /** Replaces all items and the totals in one atomic write (Daily Itinerary remove, Add Activity). */
    suspend fun replaceItems(tripId: String, items: List<ItineraryItem>, cost: TripCost): Result<Unit>

    /** Adds or updates one expense and stores the new trip total in the same write. Returns the expense id. */
    suspend fun saveExpense(tripId: String, expense: Expense, totalCostSar: Int): Result<String>

    /** Removes one expense and stores the new trip total in the same write. */
    suspend fun deleteExpense(tripId: String, expenseId: String, totalCostSar: Int): Result<Unit>

    suspend fun renameTrip(tripId: String, name: String): Result<Unit>

    suspend fun deleteTrip(tripId: String): Result<Unit>
}

/**
 * Trips live under /users/{uid}/trips/{tripId}. Only the signed-in user can read or write them (security rules).
 *
 * Writes update the local cache at once, so every screen that listens to the trip redraws immediately.
 * The repository then waits briefly for the server: fast errors (for example permission denied) are reported,
 * but the UI is never blocked while offline because the write stays queued and is sent later.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FirebaseTripRepository(
    private val database: FirebaseDatabase,
    private val authRepository: AuthRepository,
) : TripRepository {

    private val uidFlow = authRepository.currentUser.map { it?.uid }.distinctUntilChanged()

    override fun trips(): Flow<List<Trip>> = uidFlow.flatMapLatest { uid ->
        if (uid == null) {
            flowOf(emptyList())
        } else {
            database.getReference(FirebasePaths.trips(uid)).valueFlow().map { snapshot ->
                snapshot.children
                    .mapNotNull { child -> child.readOrNull<TripDto>()?.toDomain(child.key.orEmpty()) }
                    .sortedByDescending { it.updatedAt }
            }
        }
    }

    override fun trip(tripId: String): Flow<Trip?> = uidFlow.flatMapLatest { uid ->
        if (uid == null) {
            flowOf(null)
        } else {
            database.getReference(FirebasePaths.trip(uid, tripId)).valueFlow().map { snapshot ->
                if (snapshot.exists()) snapshot.readOrNull<TripDto>()?.toDomain(tripId) else null
            }
        }
    }

    override suspend fun createTrip(trip: Trip): Result<String> = runCatching {
        val uid = requireUid()
        val ref = database.getReference(FirebasePaths.trips(uid)).push()
        val tripId = requireNotNull(ref.key)
        val items = trip.items.map { it.copy(id = requireNotNull(ref.child("items").push().key)) }
        ref.setValue(trip.copy(id = tripId, items = items).toWriteMap()).awaitBriefly()
        tripId
    }

    override suspend fun replaceItems(
        tripId: String,
        items: List<ItineraryItem>,
        cost: TripCost,
    ): Result<Unit> = runCatching {
        tripRef(tripId).updateChildren(
            mapOf(
                "items" to items.toWriteMap(),
                "totalCostSar" to cost.totalSar,
                "placesCount" to items.distinctPlaceCount(),
                "updatedAt" to ServerValue.TIMESTAMP,
            ),
        ).awaitBriefly()
    }

    override suspend fun saveExpense(tripId: String, expense: Expense, totalCostSar: Int): Result<String> = runCatching {
        val ref = tripRef(tripId)
        val expenseId = expense.id.ifBlank { requireNotNull(ref.child("expenses").push().key) }
        ref.updateChildren(
            mapOf(
                "expenses/$expenseId" to expense.copy(id = expenseId).toWriteMap(),
                "totalCostSar" to totalCostSar,
                "updatedAt" to ServerValue.TIMESTAMP,
            ),
        ).awaitBriefly()
        expenseId
    }

    override suspend fun deleteExpense(tripId: String, expenseId: String, totalCostSar: Int): Result<Unit> = runCatching {
        // A null value in a multi-path update deletes that child.
        val update = hashMapOf<String, Any?>(
            "expenses/$expenseId" to null,
            "totalCostSar" to totalCostSar,
            "updatedAt" to ServerValue.TIMESTAMP,
        )
        tripRef(tripId).updateChildren(update).awaitBriefly()
    }

    override suspend fun renameTrip(tripId: String, name: String): Result<Unit> = runCatching {
        val clean = name.trim()
        require(clean.isNotEmpty()) { "Enter a name" }
        tripRef(tripId).updateChildren(
            mapOf("name" to clean.take(MAX_NAME), "updatedAt" to ServerValue.TIMESTAMP),
        ).awaitBriefly()
    }

    override suspend fun deleteTrip(tripId: String): Result<Unit> = runCatching {
        tripRef(tripId).removeValue().awaitBriefly()
    }

    private fun tripRef(tripId: String) = database.getReference(FirebasePaths.trip(requireUid(), tripId))

    private fun requireUid(): String =
        authRepository.currentUser.value?.uid ?: throw IllegalStateException("Not signed in")

    /** Reports fast server errors; gives up waiting (the write stays queued) when offline. */
    private suspend fun Task<Void>.awaitBriefly() {
        withTimeoutOrNull(WRITE_CONFIRM_TIMEOUT_MS) { await() }
    }

    private companion object {
        const val WRITE_CONFIRM_TIMEOUT_MS = 2_000L
        const val MAX_NAME = 60
    }
}
