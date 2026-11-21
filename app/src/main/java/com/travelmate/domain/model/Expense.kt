package com.travelmate.domain.model

/** Cost groups of the Trip Cost Summary. FLIGHTS only ever comes from the user's own expenses. */
enum class ExpenseCategory { FLIGHTS, STAY, TRANSPORT, FOOD, ACTIVITIES, OTHER }

/** A cost the user enters for a trip (Edit Expenses sheet), for the whole group, on top of the calculated cost. */
data class Expense(
    val id: String,
    val category: ExpenseCategory,
    val title: String,
    val amountSar: Int,
    val createdAt: Long,
)
