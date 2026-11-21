package com.travelmate.domain

import com.travelmate.domain.model.BudgetLevel
import com.travelmate.domain.model.budgetLevel
import org.junit.Assert.assertEquals
import org.junit.Test

class BudgetLevelTest {

    @Test
    fun belowNinetyPercent_isWithin() {
        assertEquals(BudgetLevel.WITHIN, budgetLevel(totalSar = 2_699, budgetSar = 3_000))
    }

    @Test
    fun ninetyToHundredPercent_isNearLimit() {
        assertEquals(BudgetLevel.NEAR_LIMIT, budgetLevel(totalSar = 2_700, budgetSar = 3_000))
        assertEquals(BudgetLevel.NEAR_LIMIT, budgetLevel(totalSar = 3_000, budgetSar = 3_000))
    }

    @Test
    fun aboveBudget_isOver() {
        assertEquals(BudgetLevel.OVER, budgetLevel(totalSar = 3_001, budgetSar = 3_000))
    }
}
