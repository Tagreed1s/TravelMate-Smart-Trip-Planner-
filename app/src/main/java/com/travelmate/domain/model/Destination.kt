package com.travelmate.domain.model

data class Destination(
    val id: String,
    val name: String,
    val country: String,
    val summary: String,
    val description: String,
    val heroImageUrl: String,
    val imageUrls: List<String>,
    val rating: Double,
    val reviewCount: Int,
    val bestTimeToVisit: String,
    val weatherRange: String,
    /** Per person per day, food and activities, low estimate. */
    val dailySpendMinSar: Int,
    val dailySpendMaxSar: Int,
    /** Per room (2 people) per night. */
    val accommodationPerNightSar: Int,
    /** Per group per day, local transport. */
    val transportPerDaySar: Int,
    val lat: Double,
    val lng: Double,
    val tips: List<String>,
    val sortOrder: Int,
)
