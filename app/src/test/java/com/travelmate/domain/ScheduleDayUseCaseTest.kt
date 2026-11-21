package com.travelmate.domain

import com.travelmate.domain.model.PlaceType
import com.travelmate.domain.usecase.PlanningRules
import com.travelmate.domain.usecase.ScheduleDayUseCase
import com.travelmate.domain.usecase.ScheduleEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScheduleDayUseCaseTest {

    private val schedule = ScheduleDayUseCase()

    private fun entry(id: String, durationMin: Int) =
        ScheduleEntry(id = id, placeId = "p-$id", type = PlaceType.ATTRACTION, durationMin = durationMin, priceSar = 10)

    @Test
    fun firstItem_startsAtNine() {
        val result = schedule(day = 2, entries = listOf(entry("a", 90)))
        val item = result.items.single()
        assertEquals(9 * 60, item.startMin)
        assertEquals(9 * 60 + 90, item.endMin)
        assertEquals(2, item.day)
        assertEquals(0, item.order)
    }

    @Test
    fun items_areSeparatedBySixtyMinutes() {
        val items = schedule(day = 1, entries = listOf(entry("a", 60), entry("b", 120), entry("c", 30))).items
        assertEquals(listOf(540, 660, 840), items.map { it.startMin })
        assertEquals(listOf(600, 780, 870), items.map { it.endMin })
        assertEquals(listOf(0, 1, 2), items.map { it.order })
    }

    @Test
    fun nothingEndsAfterTenPm_andSkippedEntriesAreReported() {
        // 09:00-13:00, 14:00-18:00, then 19:00-23:00 would end after 22:00.
        val result = schedule(day = 1, entries = listOf(entry("a", 240), entry("b", 240), entry("c", 240), entry("d", 60)))
        assertTrue(result.items.all { it.endMin <= PlanningRules.DAY_END_MIN })
        assertEquals(listOf("c"), result.didNotFit.map { it.id })
        // "d" still fits after the skipped entry: 19:00-20:00.
        assertEquals(listOf("a", "b", "d"), result.items.map { it.id })
        assertEquals(19 * 60, result.items.last().startMin)
        assertEquals(2, result.items.last().order)
    }

    @Test
    fun noEntries_giveNoItems() {
        val result = schedule(day = 1, entries = emptyList())
        assertTrue(result.items.isEmpty())
        assertTrue(result.didNotFit.isEmpty())
    }
}
