package com.travelmate.domain.usecase

import com.travelmate.domain.model.TripRequest
import java.time.LocalDate

enum class TripField { DESTINATION, START_DATE, DAYS, TRAVELERS, BUDGET, INTERESTS }

/** Checks the Plan Trip input. Returns field -> message; an empty map means the input is valid. */
class ValidateTripInputUseCase {

    /** [today] is passed in (not read from the clock) so tests are deterministic. */
    operator fun invoke(request: TripRequest, today: LocalDate): Map<TripField, String> = buildMap {
        if (request.destinationId == null) put(TripField.DESTINATION, "Choose a destination")
        if (request.startDate == null || request.startDate.isBefore(today)) {
            put(TripField.START_DATE, "Pick today or a later date")
        }
        if (request.days !in PlanningRules.MIN_DAYS..PlanningRules.MAX_DAYS) put(TripField.DAYS, "1 to 14 days")
        if (request.travelers !in PlanningRules.MIN_TRAVELERS..PlanningRules.MAX_TRAVELERS) {
            put(TripField.TRAVELERS, "1 to 10 travelers")
        }
        val budget = request.budgetSar
        if (budget == null || budget <= 0 || budget > PlanningRules.MAX_BUDGET_SAR) {
            put(TripField.BUDGET, "Enter a budget")
        }
        if (request.interests.isEmpty()) put(TripField.INTERESTS, "Pick at least one")
    }
}
