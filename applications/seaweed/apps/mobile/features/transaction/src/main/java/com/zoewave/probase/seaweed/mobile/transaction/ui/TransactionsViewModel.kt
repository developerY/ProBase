package com.zoewave.probase.seaweed.mobile.transaction.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.seaweed.data.TransactionRepository
import com.zoewave.probase.seaweed.model.HabitInsight
import com.zoewave.probase.seaweed.model.SpendingPeriod
import com.zoewave.probase.seaweed.model.Transaction
import com.zoewave.probase.seaweed.model.TrendPoint
import com.zoewave.probase.seaweed.model.navigation.TransactionTab
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.WeekFields
import java.util.Locale
import javax.inject.Inject

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow<String?>(null)
    private val _selectedTransactionId = MutableStateFlow<String?>(null)
    private val _selectedTab = MutableStateFlow(TransactionTab.RECENT)
    private var isInitialized = false

    fun setInitialCategory(category: String?) {
        if (!isInitialized && category != null) {
            _selectedCategory.value = category
            isInitialized = true
        }
    }

    fun setInitialTab(tab: TransactionTab) {
        if (!isInitialized) {
            _selectedTab.value = tab
            isInitialized = true
        }
    }

    private val _selectedTransaction = _selectedTransactionId.flatMapLatest { id ->
        if (id == null) flowOf(null) else repository.getTransaction(id)
    }

    val uiState: StateFlow<TransactionsUiState> = combine(
        repository.getAllTransactions(),
        _selectedCategory,
        _selectedTransactionId,
        _selectedTransaction,
        _selectedTab
    ) { transactions, selectedCategory, selectedTransactionId, selectedTransaction, selectedTab ->
        val categories = transactions.map { it.category }.distinct().sorted()
        val filteredTransactions = if (selectedCategory == null) {
            transactions
        } else {
            transactions.filter { it.category == selectedCategory }
        }

        val trends = calculateTrends(transactions)
        val insights = calculateHabitInsights(transactions)

        TransactionsUiState.Success(
            transactions = transactions,
            filteredTransactions = filteredTransactions,
            categories = categories,
            selectedCategory = selectedCategory,
            selectedTransactionId = selectedTransactionId,
            selectedTransaction = selectedTransaction,
            selectedTab = selectedTab,
            spendingTrends = trends,
            habitInsights = insights
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TransactionsUiState.Loading
    )

    fun onEvent(event: TransactionsUiEvent) {
        when (event) {
            is TransactionsUiEvent.DeleteTransaction -> {
                viewModelScope.launch {
                    repository.deleteTransaction(event.id)
                }
            }
            is TransactionsUiEvent.SelectCategory -> {
                _selectedCategory.value = event.category
            }
            is TransactionsUiEvent.SelectTransaction -> {
                _selectedTransactionId.value = event.id
            }
            is TransactionsUiEvent.SelectTab -> {
                _selectedTab.value = event.tab
            }
        }
    }

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
                    timestamp = dailyTransactions.first().first.date
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
                    timestamp = weeklyTransactions.minOf { it.first.date }
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
                    timestamp = monthlyTransactions.minOf { it.first.date }
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
                // Focus on high-frequency habits (at least 4 times in 30 days)
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
