package com.zoewave.probase.seaweed.model

import kotlinx.serialization.Serializable

@Serializable
data class TrendPoint(
    val label: String,
    val value: Double,
    val timestamp: Long,
    val transactionCount: Int = 0,
    val topCategory: String? = null
)

@Serializable
data class HabitInsight(
    val category: String,
    val frequency: Int,
    val totalAmount: Double,
    val dailyAverage: Double,
    val trendMessage: String,
    val budgetLimit: Double? = null
)

enum class SpendingPeriod {
    DAILY, WEEKLY, MONTHLY
}
