package com.travelmate.domain

import com.travelmate.domain.model.PlaceType
import com.travelmate.domain.model.TripRequest
import com.travelmate.domain.usecase.TripField
import com.travelmate.domain.usecase.ValidateTripInputUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ValidateTripInputUseCaseTest {

    private val validate = ValidateTripInputUseCase()
    private val today = LocalDate.of(2026, 10, 14)
    private val valid = TripRequest(
        destinationId = "sample-a",
        startDate = today,
        days = 3,
        travelers = 2,
        budgetSar = 3_000,
        interests = setOf(PlaceType.ATTRACTION),
    )

    @Test
    fun validInput_hasNoErrors() {
        assertTrue(validate(valid, today).isEmpty())
        assertTrue(validate(valid.copy(days = 14, travelers = 10, budgetSar = 1_000_000), today).isEmpty())
    }

    @Test
    fun missingDestination() {
        assertEquals(mapOf(TripField.DESTINATION to "Choose a destination"), validate(valid.copy(destinationId = null), today))
    }

    @Test
    fun startDate_missingOrInThePast() {
        val expected = mapOf(TripField.START_DATE to "Pick today or a later date")
        assertEquals(expected, validate(valid.copy(startDate = null), today))
        assertEquals(expected, validate(valid.copy(startDate = today.minusDays(1)), today))
    }

    @Test
    fun days_outsideOneToFourteen() {
        val expected = mapOf(TripField.DAYS to "1 to 14 days")
        assertEquals(expected, validate(valid.copy(days = 0), today))
        assertEquals(expected, validate(valid.copy(days = 15), today))
    }

    @Test
    fun travelers_outsideOneToTen() {
        val expected = mapOf(TripField.TRAVELERS to "1 to 10 travelers")
        assertEquals(expected, validate(valid.copy(travelers = 0), today))
        assertEquals(expected, validate(valid.copy(travelers = 11), today))
    }

    @Test
    fun budget_missingZeroOrTooHigh() {
        val expected = mapOf(TripField.BUDGET to "Enter a budget")
        assertEquals(expected, validate(valid.copy(budgetSar = null), today))
        assertEquals(expected, validate(valid.copy(budgetSar = 0), today))
        assertEquals(expected, validate(valid.copy(budgetSar = 1_000_001), today))
    }

    @Test
    fun interests_empty() {
        assertEquals(mapOf(TripField.INTERESTS to "Pick at least one"), validate(valid.copy(interests = emptySet()), today))
    }
}
