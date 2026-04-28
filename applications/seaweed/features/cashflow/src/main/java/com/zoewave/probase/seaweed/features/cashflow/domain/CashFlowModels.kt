package com.zoewave.probase.seaweed.features.cashflow.domain

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class CashFlowSummary(
    val month: LocalDate,
    val incomeCents: Long,
    val expensesCents: Long,
    val netBalanceCents: Long,
    val savingsRate: Float, // 0.0 to 1.0
    val dailyPaceCents: Long,
    val projectedEndBalanceCents: Long
)

@Serializable
data class CashFlowTrend(
    val summaries: List<CashFlowSummary>
)

sealed interface CashFlowAwareness {
    data class Positive(val message: String, val savingsPotentialCents: Long) : CashFlowAwareness
    data class Warning(val message: String, val daysUntilOverrun: Int) : CashFlowAwareness
    data class Neutral(val message: String) : CashFlowAwareness
}
