package com.zoewave.probase.seaweed.model

import kotlinx.serialization.Serializable

@Serializable
enum class ExpenseFrequency {
    WEEKLY,
    BI_WEEKLY,
    MONTHLY,
    YEARLY
}

@Serializable
enum class ExpenseCategory {
    HOUSING,
    UTILITIES,
    COMMUNICATION,
    TRANSPORTATION,
    SUBSCRIPTIONS,
    OTHER
}

@Serializable
data class RecurringExpense(
    val id: String,
    val name: String,
    val amount: Double,
    val frequency: ExpenseFrequency,
    val category: ExpenseCategory,
    val isDefault: Boolean = false,
    val nextBillingDate: Long? = null,
    val importance: SpendingImportance = SpendingImportance.REQUIRED
) {
    val monthlyImpact: Double
        get() = when (frequency) {
            ExpenseFrequency.WEEKLY -> (amount * 52) / 12
            ExpenseFrequency.BI_WEEKLY -> (amount * 26) / 12
            ExpenseFrequency.MONTHLY -> amount
            ExpenseFrequency.YEARLY -> amount / 12
        }
}
