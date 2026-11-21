package com.travelmate.data.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.travelmate.data.dto.FavoriteDto
import com.travelmate.data.dto.readOrNull
import com.travelmate.data.firebase.FirebasePaths
import com.travelmate.data.firebase.valueFlow
import com.travelmate.domain.model.FavoriteRef
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

/** Saved destinations and places of the signed-in user (hearts and the Saved screen). */
interface FavoriteRepository {
    /** Saved destinations, newest first. */
    fun favoriteDestinations(): Flow<List<FavoriteRef>>

    /** Saved places, newest first. */
    fun favoritePlaces(): Flow<List<FavoriteRef>>

    suspend fun setDestinationFavorite(destinationId: String, favorite: Boolean): Result<Unit>

    suspend fun setPlaceFavorite(placeId: String, destinationId: String, favorite: Boolean): Result<Unit>
}

/**
 * Stored as /users/{uid}/favoriteDestinations/{destinationId} and /users/{uid}/favoritePlaces/{placeId},
 * each with { destinationId, addedAt }. These are the FavoriteDestination and FavoritePlace tables of the
 * Milestone 3-A design.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FirebaseFavoriteRepository(
    private val database: FirebaseDatabase,
    private val authRepository: AuthRepository,
) : FavoriteRepository {

    private val uidFlow = authRepository.currentUser.map { it?.uid }.distinctUntilChanged()

    override fun favoriteDestinations(): Flow<List<FavoriteRef>> = listen(FirebasePaths::favoriteDestinations)

    override fun favoritePlaces(): Flow<List<FavoriteRef>> = listen(FirebasePaths::favoritePlaces)

    override suspend fun setDestinationFavorite(destinationId: String, favorite: Boolean): Result<Unit> =
        write(FirebasePaths.favoriteDestinations(requireUid()) + "/" + destinationId, destinationId, favorite)

    override suspend fun setPlaceFavorite(placeId: String, destinationId: String, favorite: Boolean): Result<Unit> =
        write(FirebasePaths.favoritePlaces(requireUid()) + "/" + placeId, destinationId, favorite)

    private fun listen(path: (String) -> String): Flow<List<FavoriteRef>> = uidFlow.flatMapLatest { uid ->
        if (uid == null) {
            flowOf(emptyList())
        } else {
            database.getReference(path(uid)).valueFlow().map { snapshot ->
                snapshot.children
                    .mapNotNull { child ->
                        val key = child.key ?: return@mapNotNull null
                        val dto = child.readOrNull<FavoriteDto>() ?: return@mapNotNull null
                        FavoriteRef(id = key, destinationId = dto.destinationId.ifBlank { key }, addedAt = dto.addedAt)
                    }
                    .sortedByDescending { it.addedAt }
            }
        }
    }

    private suspend fun write(path: String, destinationId: String, favorite: Boolean): Result<Unit> = runCatching {
        val ref = database.getReference(path)
        val task = if (favorite) {
            ref.setValue(mapOf("destinationId" to destinationId, "addedAt" to ServerValue.TIMESTAMP))
        } else {
            ref.removeValue()
        }
        task.awaitBriefly()
    }

    private fun requireUid(): String =
        authRepository.currentUser.value?.uid ?: throw IllegalStateException("Not signed in")

    private suspend fun Task<Void>.awaitBriefly() {
        withTimeoutOrNull(2_000L) { await() }
    }
}
