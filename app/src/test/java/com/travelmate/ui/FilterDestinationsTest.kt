package com.travelmate.ui

import com.travelmate.fakes.SampleData
import com.travelmate.ui.screens.explore.filterDestinations
import org.junit.Assert.assertEquals
import org.junit.Test

class FilterDestinationsTest {

    private val all = listOf(
        SampleData.destination(id = "a", name = "SAMPLE Alpha"),
        SampleData.destination(id = "b", name = "SAMPLE Beta"),
    )

    @Test
    fun blankQuery_keepsEverything() {
        assertEquals(all, filterDestinations(all, "  "))
    }

    @Test
    fun matchesNameIgnoringCase() {
        assertEquals(listOf("b"), filterDestinations(all, "bEtA").map { it.id })
    }

    @Test
    fun matchesCountry() {
        assertEquals(2, filterDestinations(all, "sample country").size)
    }

    @Test
    fun noMatch_givesEmptyList() {
        assertEquals(0, filterDestinations(all, "Gamma").size)
    }
}
