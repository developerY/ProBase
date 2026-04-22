package com.zoewave.probase.seaweed.mobile.transaction.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.seaweed.data.BudgetTargetRepository
import com.zoewave.probase.seaweed.data.TransactionRepository
import com.zoewave.probase.seaweed.model.BudgetTarget
import com.zoewave.probase.seaweed.model.HabitInsight
import com.zoewave.probase.seaweed.model.SpendingPeriod
import com.zoewave.probase.seaweed.model.Transaction
import com.zoewave.probase.seaweed.model.TrendPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale
import javax.inject.Inject
import kotlin.math.absoluteValue

data class AnalyticsUiState(
    val isLoading: Boolean = true,
    val spendingTrends: Map<SpendingPeriod, List<TrendPoint>> = emptyMap(),
    val habitInsights: List<HabitInsight> = emptyList(),
    val heatmapData: Map<LocalDate, Double> = emptyMap(),
    val allTransactions: List<Transaction> = emptyList(),
)

sealed interface AnalyticsUiEvent {
    object OnBackClicked : AnalyticsUiEvent
}

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val budgetRepository: BudgetTargetRepository
) : ViewModel() {

    val uiState: StateFlow<AnalyticsUiState> = combine(
        repository.getAllTransactions(),
        budgetRepository.getAllBudgets()
    ) { transactions, budgets ->
        AnalyticsUiState(
            isLoading = false,
            spendingTrends = calculateTrends(transactions),
            habitInsights = calculateHabitInsights(transactions, budgets),
            heatmapData = calculateHeatmapData(transactions),
            allTransactions = transactions
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AnalyticsUiState()
    )

    fun calculateTrendsForTransactions(transactions: List<Transaction>): Map<SpendingPeriod, List<TrendPoint>> {
        return calculateTrends(transactions)
    }

    fun calculateHeatmapDataForTransactions(transactions: List<Transaction>): Map<LocalDate, Double> {
        return calculateHeatmapData(transactions)
    }

    private fun calculateTrends(transactions: List<Transaction>): Map<SpendingPeriod, List<TrendPoint>> {
        val zoneId = ZoneId.systemDefault()
        val expenses = transactions.filter { it.amount < 0 }
        
        // Daily Trends (last 7 days)
        val daily = expenses
            .map { it to Instant.ofEpochMilli(it.date).atZone(zoneId).toLocalDate() }
            .groupBy { it.second }
            .map { (date, dailyTransactions) ->
                TrendPoint(
                    label = date.format(DateTimeFormatter.ofPattern("MMM dd")),
                    value = dailyTransactions.sumOf { it.first.amount }.absoluteValue,
                    timestamp = dailyTransactions.first().first.date,
                    transactionCount = dailyTransactions.size,
                    topCategory = dailyTransactions.groupBy { it.first.category }.maxByOrNull { it.value.size }?.key
                )
            }.sortedBy { it.timestamp }.takeLast(7)

        // Weekly Trends (last 4 weeks)
        val weekFields = WeekFields.of(Locale.getDefault())
        val weekly = expenses
            .map { it to Instant.ofEpochMilli(it.date).atZone(zoneId).toLocalDate() }
            .groupBy { it.second.get(weekFields.weekOfWeekBasedYear()) }
            .map { (week, weeklyTransactions) ->
                TrendPoint(
                    label = "Week $week",
                    value = weeklyTransactions.sumOf { it.first.amount }.absoluteValue,
                    timestamp = weeklyTransactions.minOf { it.first.date },
                    transactionCount = weeklyTransactions.size,
                    topCategory = weeklyTransactions.groupBy { it.first.category }.maxByOrNull { it.value.size }?.key
                )
            }.sortedBy { it.timestamp }.takeLast(4)

        // Monthly Trends (last 6 months)
        val monthly = expenses
            .map { it to Instant.ofEpochMilli(it.date).atZone(zoneId).toLocalDate() }
            .groupBy { it.second.month }
            .map { (month, monthlyTransactions) ->
                TrendPoint(
                    label = month.name.lowercase(Locale.ROOT).replaceFirstChar { it.uppercase() }.take(3),
                    value = monthlyTransactions.sumOf { it.first.amount }.absoluteValue,
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

    private fun calculateHabitInsights(
        transactions: List<Transaction>,
        budgets: List<BudgetTarget>
    ): List<HabitInsight> {
        val expenses = transactions.filter { it.amount < 0 }
        val zoneId = ZoneId.systemDefault()
        val now = LocalDate.now(zoneId)
        val thirtyDaysAgo = now.minusDays(30)
        
        val recentTransactions = expenses.filter {
            Instant.ofEpochMilli(it.date).atZone(zoneId).toLocalDate().isAfter(thirtyDaysAgo)
        }

        val transactionCategories = recentTransactions.groupBy { it.category }
        val budgetCategories = budgets.associateBy { it.categoryName }
        val allCategoryNames = (transactionCategories.keys + budgetCategories.keys).distinct()

        return allCategoryNames.map { category ->
            val categoryTransactions = transactionCategories[category] ?: emptyList()
            val frequency = categoryTransactions.size
            val totalAmount = categoryTransactions.sumOf { it.amount }.absoluteValue
            val dailyAverage = totalAmount / 30.0
            
            val trendMessage = when {
                frequency == 0 -> "No spending in the last 30 days."
                dailyAverage > 10.0 -> "This habit costs you over $${String.format(Locale.getDefault(), "%.0f", dailyAverage * 365 / 12)} per month!"
                frequency > 15 -> "You're doing this almost every other day."
                else -> "Frequent spending in this category."
            }

            HabitInsight(
                category = category,
                frequency = frequency,
                totalAmount = totalAmount,
                dailyAverage = dailyAverage,
                trendMessage = trendMessage,
                budgetLimit = budgetCategories[category]?.limitAmount
            )
        }.sortedByDescending { it.totalAmount }
    }

    private fun calculateHeatmapData(transactions: List<Transaction>): Map<LocalDate, Double> {
        val zoneId = ZoneId.systemDefault()
        return transactions.filter { it.amount < 0 }
            .map { it to Instant.ofEpochMilli(it.date).atZone(zoneId).toLocalDate() }
            .groupBy { it.second }
            .mapValues { (_, dailyTransactions) ->
                dailyTransactions.sumOf { it.first.amount }.absoluteValue
            }
    }
}
