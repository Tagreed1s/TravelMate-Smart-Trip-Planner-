package com.travelmate.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseException
import com.google.firebase.database.FirebaseDatabase
import com.travelmate.data.dto.DestinationDto
import com.travelmate.data.dto.PlaceDto
import com.travelmate.data.dto.readOrNull
import com.travelmate.data.dto.toDomain
import com.travelmate.data.firebase.FirebasePaths
import com.travelmate.data.firebase.valueFlow
import com.travelmate.domain.model.Destination
import com.travelmate.domain.model.Place
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.shareIn
import java.util.concurrent.ConcurrentHashMap

interface CatalogRepository {
    /** Sorted by sortOrder, then name. */
    fun destinations(): Flow<List<Destination>>
    fun destination(id: String): Flow<Destination?>
    fun places(destinationId: String): Flow<List<Place>>
}

/**
 * Reads the read-only catalog from the Realtime Database.
 *
 * Results are shared so every screen uses one Firebase listener per node. The shared flows carry a
 * [Result] so a read error reaches the collectors (and is shown) instead of crashing the app scope.
 *
 * The rules only allow signed-in users to read the catalog, so listeners exist only while someone is
 * signed in. Right after sign-in the database connection can still be unauthenticated for a moment,
 * which makes the first listen fail with "Permission denied"; such failures are retried a few times.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FirebaseCatalogRepository(
    private val database: FirebaseDatabase,
    private val authRepository: AuthRepository,
    private val appScope: CoroutineScope,
) : CatalogRepository {

    private val sharedDestinations: SharedFlow<Result<List<Destination>>> =
        signedInListen(FirebasePaths.DESTINATIONS) { snapshot ->
            snapshot.children
                .mapNotNull { child -> child.readOrNull<DestinationDto>()?.toDomain(child.key.orEmpty()) }
                .sortedWith(compareBy<Destination> { it.sortOrder }.thenBy { it.name })
        }

    private val sharedPlaces = ConcurrentHashMap<String, SharedFlow<Result<List<Place>>>>()

    override fun destinations(): Flow<List<Destination>> = sharedDestinations.map { it.getOrThrow() }

    override fun destination(id: String): Flow<Destination?> =
        destinations().map { list -> list.firstOrNull { it.id == id } }.distinctUntilChanged()

    override fun places(destinationId: String): Flow<List<Place>> =
        sharedPlaces.getOrPut(destinationId) {
            signedInListen(FirebasePaths.places(destinationId)) { snapshot ->
                snapshot.children.mapNotNull { child ->
                    child.readOrNull<PlaceDto>()?.toDomain(child.key.orEmpty(), destinationId)
                }
            }
        }.map { it.getOrThrow() }

    /**
     * Listens to [path] while a user is signed in (a new listener per user) and shares the result.
     * The replay cache is cleared when nobody is listening, so an old error is never shown again.
     */
    private fun <T> signedInListen(path: String, transform: (DataSnapshot) -> T): SharedFlow<Result<T>> =
        authRepository.currentUser
            .map { it?.uid }
            .distinctUntilChanged()
            .flatMapLatest { uid ->
                if (uid == null) {
                    emptyFlow()
                } else {
                    database.getReference(path).valueFlow()
                        .retryWhen { cause, attempt ->
                            val retry = cause is DatabaseException && attempt < MAX_RETRIES
                            if (retry) delay(RETRY_DELAY_MS * (attempt + 1))
                            retry
                        }
                        .map { Result.success(transform(it)) }
                        .catch { emit(Result.failure(it)) }
                }
            }
            .shareIn(appScope, SharingStarted.WhileSubscribed(5_000, replayExpirationMillis = 0), replay = 1)

    private companion object {
        const val MAX_RETRIES = 3L
        const val RETRY_DELAY_MS = 500L
    }
}
