package com.zoewave.probase.seaweed.mobile.transaction.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.seaweed.data.TransactionRepository
import com.zoewave.probase.seaweed.model.HabitInsight
import com.zoewave.probase.seaweed.model.SpendingPeriod
import com.zoewave.probase.seaweed.model.Transaction
import com.zoewave.probase.seaweed.model.TrendPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale
import javax.inject.Inject

data class AnalyticsUiState(
    val isLoading: Boolean = true,
    val spendingTrends: Map<SpendingPeriod, List<TrendPoint>> = emptyMap(),
    val habitInsights: List<HabitInsight> = emptyList(),
)

sealed interface AnalyticsUiEvent {
    object OnBackClicked : AnalyticsUiEvent
}

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    val uiState: StateFlow<AnalyticsUiState> = repository.getAllTransactions()
        .map { transactions ->
            AnalyticsUiState(
                isLoading = false,
                spendingTrends = calculateTrends(transactions),
                habitInsights = calculateHabitInsights(transactions)
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AnalyticsUiState()
        )

    private fun calculateTrends(transactions: List<Transaction>): Map<SpendingPeriod, List<TrendPoint>> {
        val zoneId = ZoneId.systemDefault()
        
        // Daily Trends (last 7 days)
        val daily = transactions
            .map { it to Instant.ofEpochMilli(it.date).atZone(zoneId).toLocalDate() }
            .groupBy { it.second }
            .map { (date, dailyTransactions) ->
                TrendPoint(
                    label = date.format(DateTimeFormatter.ofPattern("MMM dd")),
                    value = dailyTransactions.sumOf { it.first.amount },
                    timestamp = dailyTransactions.first().first.date,
                    transactionCount = dailyTransactions.size,
                    topCategory = dailyTransactions.groupBy { it.first.category }.maxByOrNull { it.value.size }?.key
                )
            }.sortedBy { it.timestamp }.takeLast(7)

        // Weekly Trends (last 4 weeks)
        val weekFields = WeekFields.of(Locale.getDefault())
        val weekly = transactions
            .map { it to Instant.ofEpochMilli(it.date).atZone(zoneId).toLocalDate() }
            .groupBy { it.second.get(weekFields.weekOfWeekBasedYear()) }
            .map { (week, weeklyTransactions) ->
                TrendPoint(
                    label = "Week $week",
                    value = weeklyTransactions.sumOf { it.first.amount },
                    timestamp = weeklyTransactions.minOf { it.first.date },
                    transactionCount = weeklyTransactions.size,
                    topCategory = weeklyTransactions.groupBy { it.first.category }.maxByOrNull { it.value.size }?.key
                )
            }.sortedBy { it.timestamp }.takeLast(4)

        // Monthly Trends (last 6 months)
        val monthly = transactions
            .map { it to Instant.ofEpochMilli(it.date).atZone(zoneId).toLocalDate() }
            .groupBy { it.second.month }
            .map { (month, monthlyTransactions) ->
                TrendPoint(
                    label = month.name.lowercase(Locale.ROOT).replaceFirstChar { it.uppercase() }.take(3),
                    value = monthlyTransactions.sumOf { it.first.amount },
                    timestamp = monthlyTransactions.minOf { it.first.date },
                    transactionCount = monthlyTransactions.size,
                    topCategory = monthlyTransactions.groupBy { it.first.category }.maxByOrNull { it.value.size }?.key
                )
            }.sortedBy { it.timestamp }.takeLast(6)

        return mapOf(
            SpendingPeriod.DAILY to daily,
            SpendingPeriod.WEEKLY to weekly,
            SpendingPeriod.MONTHLY to monthly
        )
    }

    private fun calculateHabitInsights(transactions: List<Transaction>): List<HabitInsight> {
        if (transactions.isEmpty()) return emptyList()
        
        val zoneId = ZoneId.systemDefault()
        val now = LocalDate.now(zoneId)
        val thirtyDaysAgo = now.minusDays(30)
        
        val recentTransactions = transactions.filter {
            Instant.ofEpochMilli(it.date).atZone(zoneId).toLocalDate().isAfter(thirtyDaysAgo)
        }

        return recentTransactions.groupBy { it.category }
            .mapNotNull { (category, categoryTransactions) ->
                val frequency = categoryTransactions.size
                if (frequency < 4) return@mapNotNull null
                
                val totalAmount = categoryTransactions.sumOf { it.amount }
                val dailyAverage = totalAmount / 30.0
                
                val trendMessage = when {
                    dailyAverage > 10.0 -> "This habit costs you over $${String.format(Locale.getDefault(), "%.0f", dailyAverage * 365 / 12)} per month!"
                    frequency > 15 -> "You're doing this almost every other day."
                    else -> "Frequent spending in this category."
                }

                HabitInsight(
                    category = category,
                    frequency = frequency,
                    totalAmount = totalAmount,
                    dailyAverage = dailyAverage,
                    trendMessage = trendMessage
                )
            }.sortedByDescending { it.totalAmount }
    }
}
