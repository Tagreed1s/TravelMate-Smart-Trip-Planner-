package com.travelmate.domain.model

/**
 * A saved destination or place (Saved screen, hearts).
 * [id] is the destination id or the place id; for a saved destination [destinationId] equals [id].
 */
data class FavoriteRef(
    val id: String,
    val destinationId: String,
    val addedAt: Long,
)
