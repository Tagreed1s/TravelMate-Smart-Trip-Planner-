package com.travelmate.domain.model

data class TripCost(
    val accommodationSar: Int,
    val transportSar: Int,
    val activitiesSar: Int,
    val foodSar: Int,
    /** Sum of the user's own expenses (Edit Expenses), for example flights. */
    val extrasSar: Int = 0,
) {
    val totalSar: Int get() = accommodationSar + transportSar + activitiesSar + foodSar + extrasSar
    fun remainingSar(budgetSar: Int) = budgetSar - totalSar
}

/** Color level of the budget bar: below 90 percent, 90 to 100 percent, above 100 percent. */
enum class BudgetLevel { WITHIN, NEAR_LIMIT, OVER }

fun budgetLevel(totalSar: Int, budgetSar: Int): BudgetLevel = when {
    budgetSar <= 0 -> if (totalSar > 0) BudgetLevel.OVER else BudgetLevel.WITHIN
    totalSar > budgetSar -> BudgetLevel.OVER
    totalSar * 10L >= budgetSar * 9L -> BudgetLevel.NEAR_LIMIT
    else -> BudgetLevel.WITHIN
}
