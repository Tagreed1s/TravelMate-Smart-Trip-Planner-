package com.travelmate.data.dto

/**
 * Mirrors /catalog/destinations/{destinationId}. Every field is a `var` with a default value,
 * so Firebase can create it with the no-argument constructor (DataSnapshot.getValue).
 * The team's catalog.json uses exactly these keys, so no @PropertyName mapping is needed (docs/DATA_SCHEMA.md).
 */
data class DestinationDto(
    var id: String = "",
    var name: String = "",
    var country: String = "",
    var summary: String = "",
    var description: String = "",
    var heroImageUrl: String = "",
    var imageUrls: List<String> = emptyList(),
    var rating: Double = 0.0,
    var reviewCount: Int = 0,
    var bestTimeToVisit: String = "",
    var weatherRange: String = "",
    var dailySpendMinSar: Int = 0,
    var dailySpendMaxSar: Int = 0,
    var accommodationPerNightSar: Int = 0,
    var transportPerDaySar: Int = 0,
    var lat: Double = 0.0,
    var lng: Double = 0.0,
    var tips: List<String> = emptyList(),
    var sortOrder: Int = 0,
)

/** Mirrors /catalog/places/{destinationId}/{placeId}. */
data class PlaceDto(
    var id: String = "",
    var destinationId: String = "",
    var type: String = "",
    var name: String = "",
    var description: String = "",
    var imageUrl: String = "",
    var rating: Double = 0.0,
    var priceSar: Int = 0,
    var durationMin: Int = 0,
    var lat: Double = 0.0,
    var lng: Double = 0.0,
    var openingHours: String = "",
    var schedule: String? = null,
)
