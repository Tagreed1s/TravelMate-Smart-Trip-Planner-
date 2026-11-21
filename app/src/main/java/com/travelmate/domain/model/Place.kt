package com.travelmate.domain.model

enum class PlaceType { ATTRACTION, RESTAURANT, ACTIVITY, EVENT }

data class Place(
    val id: String,
    val destinationId: String,
    val type: PlaceType,
    val name: String,
    val description: String,
    val imageUrl: String,
    val rating: Double,
    /** Per person, 0 if free. */
    val priceSar: Int,
    /** Typical visit length in minutes. */
    val durationMin: Int,
    val lat: Double,
    val lng: Double,
    val openingHours: String,
    /** EVENT only. */
    val schedule: String?,
)
