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
    val averageAmountCents: Long,
    val frequency: ExpenseFrequency,
    val categoryId: String,
    val isDefault: Boolean = false,
    val nextBillingDate: Long? = null,
    val defaultType: SpendingType = SpendingType.NEED,
    val cardId: String? = null
) {
    val monthlyImpactCents: Long
        get() = when (frequency) {
            ExpenseFrequency.WEEKLY -> (averageAmountCents * 52) / 12
            ExpenseFrequency.BI_WEEKLY -> (averageAmountCents * 26) / 12
            ExpenseFrequency.MONTHLY -> averageAmountCents
            ExpenseFrequency.YEARLY -> averageAmountCents / 12
        }
}
