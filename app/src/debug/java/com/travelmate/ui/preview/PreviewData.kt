package com.travelmate.ui.preview

import com.travelmate.domain.model.Destination
import com.travelmate.domain.model.Expense
import com.travelmate.domain.model.ExpenseCategory
import com.travelmate.domain.model.ItineraryItem
import com.travelmate.domain.model.Place
import com.travelmate.domain.model.PlaceType
import com.travelmate.domain.model.Trip
import com.travelmate.domain.model.TripCost
import java.time.LocalDate

/**
 * SAMPLE data for Compose previews. Lives in the debug source set only, so it never reaches
 * the release build (plan rule 6). All values are obviously fake.
 */
object PreviewData {

    fun destination(id: String, name: String) = Destination(
        id = id,
        name = name,
        country = "SAMPLE Country",
        summary = "SAMPLE one-sentence summary shown on cards.",
        description = "SAMPLE description. It is long enough to show how the About section collapses " +
            "to three lines and expands with the More button. Real text comes from Firebase.",
        heroImageUrl = "",
        imageUrls = emptyList(),
        rating = 4.0,
        reviewCount = 1_000,
        bestTimeToVisit = "SAMPLE",
        weatherRange = "SAMPLE",
        dailySpendMinSar = 10,
        dailySpendMaxSar = 30,
        accommodationPerNightSar = 100,
        transportPerDaySar = 10,
        lat = 10.0,
        lng = 10.0,
        tips = listOf("SAMPLE tip one", "SAMPLE tip two", "SAMPLE tip three"),
        sortOrder = 1,
    )

    val destinationA = destination("sample-a", "SAMPLE Destination A")
    val destinationB = destination("sample-b", "SAMPLE Destination B")

    private fun place(index: Int, type: PlaceType, price: Int) = Place(
        id = "sample-place-$index",
        destinationId = destinationA.id,
        type = type,
        name = "SAMPLE Place $index",
        description = "SAMPLE place description.",
        imageUrl = "",
        rating = 4.0,
        priceSar = price,
        durationMin = 60,
        lat = 10.0,
        lng = 10.0,
        openingHours = "SAMPLE hours",
        schedule = null,
    )

    val places = listOf(
        place(1, PlaceType.ATTRACTION, 10),
        place(2, PlaceType.RESTAURANT, 20),
        place(3, PlaceType.ACTIVITY, 30),
        place(4, PlaceType.EVENT, 0),
        place(5, PlaceType.ATTRACTION, 20),
        place(6, PlaceType.RESTAURANT, 10),
    )

    private fun item(index: Int, day: Int, order: Int, place: Place) = ItineraryItem(
        id = "sample-item-$index",
        placeId = place.id,
        type = place.type,
        day = day,
        order = order,
        startMin = 540 + order * 120,
        endMin = 600 + order * 120,
        priceSar = place.priceSar,
    )

    val trip = Trip(
        id = "sample-trip",
        destinationId = destinationA.id,
        destinationName = destinationA.name,
        heroImageUrl = "",
        name = "SAMPLE Getaway",
        startDate = LocalDate.of(2026, 11, 12),
        days = 3,
        travelers = 2,
        budgetSar = 300,
        interests = setOf(PlaceType.ATTRACTION, PlaceType.ACTIVITY),
        items = listOf(
            item(1, day = 1, order = 0, place = places[0]),
            item(2, day = 1, order = 1, place = places[1]),
            item(3, day = 1, order = 2, place = places[2]),
            item(4, day = 2, order = 0, place = places[4]),
        ),
        totalCostSar = 390,
        placesCount = 4,
        createdAt = 0,
        updatedAt = 0,
        expenses = listOf(Expense("sample-expense-1", ExpenseCategory.FLIGHTS, "SAMPLE flights", 100, 1)),
    )

    val tripCost = TripCost(accommodationSar = 200, transportSar = 30, activitiesSar = 40, foodSar = 20, extrasSar = 100)
}
