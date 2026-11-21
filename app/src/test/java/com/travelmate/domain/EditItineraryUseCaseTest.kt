package com.travelmate.domain

import com.travelmate.domain.model.Expense
import com.travelmate.domain.model.ExpenseCategory
import com.travelmate.domain.model.ItineraryItem
import com.travelmate.domain.model.Place
import com.travelmate.domain.model.PlaceType
import com.travelmate.domain.model.Trip
import com.travelmate.domain.usecase.CalculateTripCostUseCase
import com.travelmate.domain.usecase.EditItineraryUseCase
import com.travelmate.domain.usecase.ItineraryEdit
import com.travelmate.domain.usecase.PlanningRules
import com.travelmate.domain.usecase.ScheduleDayUseCase
import com.travelmate.domain.usecase.ScheduleEntry
import com.travelmate.fakes.SampleData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class EditItineraryUseCaseTest {

    private val scheduleDay = ScheduleDayUseCase()
    private val edit = EditItineraryUseCase(scheduleDay, CalculateTripCostUseCase())
    private val destination = SampleData.destinationA
    private val places = SampleData.places(destination.id)
    private val durations = places.associate { it.id to it.durationMin }

    private fun trip(items: List<ItineraryItem>, expenses: List<Expense> = emptyList()) = Trip(
        id = "sample-trip",
        destinationId = destination.id,
        destinationName = destination.name,
        heroImageUrl = "",
        name = "SAMPLE Trip",
        startDate = LocalDate.of(2030, 1, 1),
        days = 2,
        travelers = 2,
        budgetSar = 10_000,
        interests = setOf(PlaceType.ATTRACTION),
        items = items,
        totalCostSar = 0,
        placesCount = 0,
        createdAt = 0,
        updatedAt = 0,
        expenses = expenses,
    )

    private fun dayItems(day: Int, vararg dayPlaces: Place): List<ItineraryItem> = scheduleDay(
        day,
        dayPlaces.mapIndexed { index, place ->
            ScheduleEntry("d$day-$index", place.id, place.type, place.durationMin, place.priceSar)
        },
    ).items

    @Test
    fun remove_reschedulesTheRestOfTheDay() {
        val day1 = dayItems(1, places[0], places[2], places[4])
        val result = edit.remove(trip(day1), day1.first().id, destination, durations)
        assertNotNull(result)

        val remaining = result!!.items
        assertEquals(2, remaining.size)
        assertEquals(PlanningRules.DAY_START_MIN, remaining.first().startMin)
        assertEquals(listOf(0, 1), remaining.map { it.order })
    }

    @Test
    fun add_putsThePlaceAtTheEndOfTheDay() {
        val day1 = dayItems(1, places[0])
        val result = edit.add(trip(day1), 1, places[2], "new-item", destination, durations)

        assertTrue(result is ItineraryEdit.Updated)
        val added = (result as ItineraryEdit.Updated).items.single { it.id == "new-item" }
        assertEquals(1, added.order)
        // 09:00 to 10:00, then a 60 minute gap.
        assertEquals(11 * 60, added.startMin)
    }

    @Test
    fun add_reportsDayFull_whenThePlaceWouldEndAfter22() {
        val long = SampleData.place("long-1", PlaceType.ACTIVITY, durationMin = 240)
        // 09:00 to 13:00 and 14:00 to 18:00; a third 4-hour visit would end at 23:00.
        val day1 = dayItems(1, long, long.copy(id = "long-2"))
        val result = edit.add(trip(day1), 1, long.copy(id = "long-3"), "new-item", destination, durations)

        assertEquals(ItineraryEdit.DayFull, result)
    }

    @Test
    fun cost_includesTheUsersExpenses() {
        val flights = Expense("e1", ExpenseCategory.FLIGHTS, "Flights", 500, 0)
        val result = edit.add(trip(emptyList(), listOf(flights)), 1, places[0], "new-item", destination, durations)

        assertEquals(500, (result as ItineraryEdit.Updated).cost.extrasSar)
    }
}
