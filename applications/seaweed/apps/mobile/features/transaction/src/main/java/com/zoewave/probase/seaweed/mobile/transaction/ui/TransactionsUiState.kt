package com.zoewave.probase.seaweed.mobile.transaction.ui

import com.zoewave.probase.seaweed.model.Transaction
import com.zoewave.probase.seaweed.model.TrendPoint
import com.zoewave.probase.seaweed.model.HabitInsight
import com.zoewave.probase.seaweed.model.navigation.TransactionTab

sealed interface TransactionsUiState {
    object Loading : TransactionsUiState
    data class Success(
        val transactions: List<Transaction> = emptyList(),
        val filteredTransactions: List<Transaction> = emptyList(),
        val categories: List<String> = emptyList(),
        val selectedCategory: String? = null,
        val selectedTransactionId: String? = null,
        val selectedTransaction: Transaction? = null,
        val selectedTab: TransactionTab = TransactionTab.RECENT,
        val spendingTrends: Map<com.zoewave.probase.seaweed.model.SpendingPeriod, List<TrendPoint>> = emptyMap(),
        val habitInsights: List<HabitInsight> = emptyList()
    ) : TransactionsUiState
}

sealed interface TransactionsUiEvent {
    data class DeleteTransaction(val id: String) : TransactionsUiEvent
    data class SelectCategory(val category: String?) : TransactionsUiEvent
    data class SelectTransaction(val id: String?) : TransactionsUiEvent
    data class SelectTab(val tab: TransactionTab) : TransactionsUiEvent
}
