package com.zoewave.probase.seaweed.features.cashflow.domain

import kotlinx.datetime.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

class CashFlowEngine @Inject constructor() {
    
    fun calculateSummary(
        incomeCents: Long,
        expensesCents: Long,
        monthDays: Int,
        currentDay: Int
    ): CashFlowSummary {
        val net = incomeCents - expensesCents
        val rate = if (incomeCents > 0) net.toFloat() / incomeCents else 0f
        val pace = if (currentDay > 0) expensesCents / currentDay else 0L
        val projection = incomeCents - (pace * monthDays)
        
        val now = LocalDateTime.now()
        
        return CashFlowSummary(
            month = LocalDate(now.year, now.monthValue, now.dayOfMonth),
            incomeCents = incomeCents,
            expensesCents = expensesCents,
            netBalanceCents = net,
            savingsRate = rate,
            dailyPaceCents = pace,
            projectedEndBalanceCents = projection
        )
    }

    fun generateAwareness(summary: CashFlowSummary): CashFlowAwareness {
        return when {
            summary.savingsRate > 0.2f -> CashFlowAwareness.Positive(
                message = "Excellent pace. You are saving ${String.format("%.0f", summary.savingsRate * 100)}% of your income.",
                savingsPotentialCents = summary.netBalanceCents
            )
            summary.projectedEndBalanceCents < 0 -> CashFlowAwareness.Warning(
                message = "Warning: Current pace exceeds income.",
                daysUntilOverrun = 5 // Placeholder logic
            )
            else -> CashFlowAwareness.Neutral(
                message = "You are currently living within your means. Keep it up."
            )
        }
    }
}
