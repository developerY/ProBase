package com.zoewave.probase.seaweed.model

import kotlinx.serialization.Serializable

@Serializable
data class TrendPoint(
    val label: String,
    val value: Double,
    val timestamp: Long
)

@Serializable
data class HabitInsight(
    val category: String,
    val frequency: Int,
    val totalAmount: Double,
    val dailyAverage: Double,
    val trendMessage: String
)

enum class SpendingPeriod {
    DAILY, WEEKLY, MONTHLY
}
