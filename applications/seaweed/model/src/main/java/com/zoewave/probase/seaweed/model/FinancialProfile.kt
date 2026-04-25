package com.zoewave.probase.seaweed.model

import kotlinx.serialization.Serializable

@Serializable
data class FinancialProfile(
    val monthlyIncomeCents: Long,
    val totalFixedCostsCents: Long,
    val realStartingBalanceCents: Long,
    val monthlyVariableSpendingCents: Long,
    val flexibleMoneyRemainingCents: Long,
    val totalBudgetedAmountCents: Long,
    val unallocatedMoneyCents: Long,
    val categoryOverviews: List<CategoryOverview>,
    val monthProgress: Float
)
