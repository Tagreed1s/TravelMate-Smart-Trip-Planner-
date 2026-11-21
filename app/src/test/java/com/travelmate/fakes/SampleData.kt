package com.travelmate.fakes

import com.travelmate.domain.model.Destination
import com.travelmate.domain.model.Place
import com.travelmate.domain.model.PlaceType

/** SAMPLE data for tests only. All values are obviously fake (plan rule 6). */
object SampleData {

    fun destination(
        id: String = "sample-a",
        name: String = "SAMPLE Destination A",
        accommodationPerNightSar: Int = 100,
        transportPerDaySar: Int = 10,
        dailySpendMinSar: Int = 10,
        dailySpendMaxSar: Int = 30,
        sortOrder: Int = 1,
    ) = Destination(
        id = id,
        name = name,
        country = "SAMPLE Country",
        summary = "SAMPLE summary.",
        description = "SAMPLE description.",
        heroImageUrl = "",
        imageUrls = emptyList(),
        rating = 4.0,
        reviewCount = 0,
        bestTimeToVisit = "SAMPLE",
        weatherRange = "SAMPLE",
        dailySpendMinSar = dailySpendMinSar,
        dailySpendMaxSar = dailySpendMaxSar,
        accommodationPerNightSar = accommodationPerNightSar,
        transportPerDaySar = transportPerDaySar,
        lat = 10.0,
        lng = 10.0,
        tips = listOf("SAMPLE tip"),
        sortOrder = sortOrder,
    )

    fun place(
        id: String,
        type: PlaceType,
        priceSar: Int = 10,
        durationMin: Int = 60,
        rating: Double = 4.0,
        lat: Double = 10.0,
        lng: Double = 10.0,
        destinationId: String = "sample-a",
    ) = Place(
        id = id,
        destinationId = destinationId,
        type = type,
        name = "SAMPLE Place $id",
        description = "SAMPLE",
        imageUrl = "",
        rating = rating,
        priceSar = priceSar,
        durationMin = durationMin,
        lat = lat,
        lng = lng,
        openingHours = "SAMPLE",
        schedule = null,
    )

    val destinationA = destination()
    val destinationB = destination(id = "sample-b", name = "SAMPLE Destination B", sortOrder = 2)

    /** 6 places: 2 restaurants and 4 others, prices 10 / 20 / 30. */
    fun places(destinationId: String) = listOf(
        place("$destinationId-attr-1", PlaceType.ATTRACTION, priceSar = 10, rating = 5.0, destinationId = destinationId),
        place("$destinationId-attr-2", PlaceType.ATTRACTION, priceSar = 20, rating = 4.0, lat = 10.1, destinationId = destinationId),
        place("$destinationId-act-1", PlaceType.ACTIVITY, priceSar = 30, rating = 4.5, lat = 10.01, destinationId = destinationId),
        place("$destinationId-event-1", PlaceType.EVENT, priceSar = 20, rating = 3.0, destinationId = destinationId),
        place("$destinationId-food-1", PlaceType.RESTAURANT, priceSar = 10, rating = 4.0, destinationId = destinationId),
        place("$destinationId-food-2", PlaceType.RESTAURANT, priceSar = 20, rating = 3.5, destinationId = destinationId),
    )
}
