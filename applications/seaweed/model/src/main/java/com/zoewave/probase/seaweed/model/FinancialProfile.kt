package com.zoewave.probase.seaweed.model

import kotlinx.serialization.Serializable

@Serializable
data class FinancialProfile(
    val monthlyIncome: Double,
    val totalFixedCosts: Double,
    val realStartingBalance: Double,
    val monthlyVariableSpending: Double,
    val flexibleMoneyRemaining: Double,
    val totalBudgetedAmount: Double,
    val unallocatedMoney: Double,
    val categoryOverviews: List<CategoryOverview>,
    val monthProgress: Float
)
