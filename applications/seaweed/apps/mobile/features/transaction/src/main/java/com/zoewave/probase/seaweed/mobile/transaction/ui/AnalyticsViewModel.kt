package com.zoewave.probase.seaweed.mobile.transaction.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.seaweed.data.BudgetTargetRepository
import com.zoewave.probase.seaweed.data.CategoryRepository
import com.zoewave.probase.seaweed.data.TransactionRepository
import com.zoewave.probase.seaweed.mobile.transaction.R
import com.zoewave.probase.seaweed.model.BudgetTarget
import com.zoewave.probase.seaweed.model.Category
import com.zoewave.probase.seaweed.model.HabitInsight
import com.zoewave.probase.seaweed.model.SpendingPeriod
import com.zoewave.probase.seaweed.model.Transaction
import com.zoewave.probase.seaweed.model.TrendPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    val heatmapData: Map<LocalDate, Long> = emptyMap(),
    val allTransactions: List<Transaction> = emptyList(),
    val categoriesMap: Map<String, Category> = emptyMap()
)

sealed interface AnalyticsUiEvent {
    object OnBackClicked : AnalyticsUiEvent
}

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val budgetRepository: BudgetTargetRepository,
    private val categoryRepository: CategoryRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val uiState: StateFlow<AnalyticsUiState> = combine(
        repository.getAllTransactions(),
        budgetRepository.getAllBudgets(),
        categoryRepository.getAllCategories()
    ) { txs, budgets, categories ->
        val categoriesMap = categories.associateBy { it.id }
        
        AnalyticsUiState(
            isLoading = false,
            spendingTrends = calculateTrends(txs, categoriesMap),
            habitInsights = calculateHabitInsights(txs, budgets, categoriesMap),
            heatmapData = calculateHeatmapData(txs),
            allTransactions = txs,
            categoriesMap = categoriesMap
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AnalyticsUiState()
    )

    fun calculateTrendsForTransactions(
        transactions: List<Transaction>,
        categoriesMap: Map<String, Category>
    ): Map<SpendingPeriod, List<TrendPoint>> {
        return calculateTrends(transactions, categoriesMap)
    }

    fun calculateHeatmapDataForTransactions(transactions: List<Transaction>): Map<LocalDate, Long> {
        return calculateHeatmapData(transactions)
    }

    private fun calculateTrends(
        transactions: List<Transaction>,
        categoriesMap: Map<String, Category>
    ): Map<SpendingPeriod, List<TrendPoint>> {
        val zoneId = ZoneId.systemDefault()
        val expenses = transactions.filter { it.amountCents < 0 }
        
        // Daily Trends (last 7 days)
        val daily = expenses
            .map { it to Instant.ofEpochMilli(it.timestamp).atZone(zoneId).toLocalDate() }
            .groupBy { it.second }
            .map { (date, dailyTransactions) ->
                val topCatId = dailyTransactions.groupBy { it.first.categoryId }.maxByOrNull { it.value.size }?.key
                TrendPoint(
                    label = date.format(DateTimeFormatter.ofPattern("MMM dd")),
                    value = dailyTransactions.sumOf { it.first.amountCents }.absoluteValue.toDouble() / 100.0,
                    timestamp = dailyTransactions.first().first.timestamp,
                    transactionCount = dailyTransactions.size,
                    topCategory = categoriesMap[topCatId]?.name ?: topCatId
                )
            }.sortedBy { it.timestamp }.takeLast(7)

        // Weekly Trends (last 4 weeks)
        val weekFields = WeekFields.of(Locale.getDefault())
        val weekly = expenses
            .map { it to Instant.ofEpochMilli(it.timestamp).atZone(zoneId).toLocalDate() }
            .groupBy { it.second.get(weekFields.weekOfWeekBasedYear()) }
            .map { (week, weeklyTransactions) ->
                val topCatId = weeklyTransactions.groupBy { it.first.categoryId }.maxByOrNull { it.value.size }?.key
                TrendPoint(
                    label = "Week $week",
                    value = weeklyTransactions.sumOf { it.first.amountCents }.absoluteValue.toDouble() / 100.0,
                    timestamp = weeklyTransactions.minOf { it.first.timestamp },
                    transactionCount = weeklyTransactions.size,
                    topCategory = categoriesMap[topCatId]?.name ?: topCatId
                )
            }.sortedBy { it.timestamp }.takeLast(4)

        // Monthly Trends (last 6 months)
        val monthly = expenses
            .map { it to Instant.ofEpochMilli(it.timestamp).atZone(zoneId).toLocalDate() }
            .groupBy { it.second.month }
            .map { (month, monthlyTransactions) ->
                val topCatId = monthlyTransactions.groupBy { it.first.categoryId }.maxByOrNull { it.value.size }?.key
                TrendPoint(
                    label = month.name.lowercase(Locale.ROOT).replaceFirstChar { it.uppercase() }.take(3),
                    value = monthlyTransactions.sumOf { it.first.amountCents }.absoluteValue.toDouble() / 100.0,
                    timestamp = monthlyTransactions.minOf { it.first.timestamp },
                    transactionCount = monthlyTransactions.size,
                    topCategory = categoriesMap[topCatId]?.name ?: topCatId
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
        budgets: List<BudgetTarget>,
        categoriesMap: Map<String, Category>
    ): List<HabitInsight> {
        val expenses = transactions.filter { it.amountCents < 0 }
        val zoneId = ZoneId.systemDefault()
        val now = LocalDate.now(zoneId)
        val thirtyDaysAgo = now.minusDays(30)
        
        val recentTransactions = expenses.filter {
            Instant.ofEpochMilli(it.timestamp).atZone(zoneId).toLocalDate().isAfter(thirtyDaysAgo)
        }

        val transactionCategories = recentTransactions.groupBy { it.categoryId }
        val budgetCategories = budgets.associateBy { it.categoryId }
        val allCategoryIds = (transactionCategories.keys + budgetCategories.keys).distinct()

        return allCategoryIds.map { categoryId ->
            val categoryTransactions = transactionCategories[categoryId] ?: emptyList()
            val frequency = categoryTransactions.size
            val totalAmountCents = categoryTransactions.sumOf { it.amountCents }.absoluteValue
            val dailyAverageCents = totalAmountCents / 30.0
            val categoryName = categoriesMap[categoryId]?.name ?: categoryId
            
            val trendMessage = when {
                frequency == 0 -> context.getString(R.string.applications_seaweed_apps_mobile_features_transaction_habit_no_spending)
                dailyAverageCents > 1000.0 -> context.getString(
                    R.string.applications_seaweed_apps_mobile_features_transaction_habit_high_cost,
                    String.format(Locale.getDefault(), "%.0f", (dailyAverageCents * 365 / 12) / 100.0)
                )
                frequency > 15 -> context.getString(R.string.applications_seaweed_apps_mobile_features_transaction_habit_frequent)
                else -> context.getString(R.string.applications_seaweed_apps_mobile_features_transaction_habit_general)
            }

            HabitInsight(
                category = categoryName,
                frequency = frequency,
                totalAmount = totalAmountCents.toDouble() / 100.0,
                dailyAverage = dailyAverageCents / 100.0,
                trendMessage = trendMessage,
                budgetLimit = budgetCategories[categoryId]?.limitAmountCents?.toDouble()?.let { it / 100.0 }
            )
        }.sortedByDescending { it.totalAmount }
    }

    private fun calculateHeatmapData(transactions: List<Transaction>): Map<LocalDate, Long> {
        val zoneId = ZoneId.systemDefault()
        return transactions.filter { it.amountCents < 0 }
            .map { it to Instant.ofEpochMilli(it.timestamp).atZone(zoneId).toLocalDate() }
            .groupBy { it.second }
            .mapValues { (_, dailyTransactions) ->
                dailyTransactions.sumOf { it.first.amountCents }.absoluteValue
            }
    }
}
