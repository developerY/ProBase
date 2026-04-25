package com.zoewave.probase.seaweed.mobile.transaction.ui

import com.zoewave.probase.seaweed.model.SpendingType
import com.zoewave.probase.seaweed.model.Transaction
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
        val selectedTab: TransactionTab = TransactionTab.RECENT
    ) : TransactionsUiState
}

sealed interface TransactionsUiEvent {
    data class DeleteTransaction(val id: String) : TransactionsUiEvent
    data class UpdateImportance(val id: String, val importance: SpendingType) : TransactionsUiEvent
    data class SelectCategory(val category: String?) : TransactionsUiEvent
    data class SelectTransaction(val id: String?) : TransactionsUiEvent
    data class SelectTab(val tab: TransactionTab) : TransactionsUiEvent
    data class NavigateTo(val destination: com.zoewave.probase.seaweed.model.navigation.SeaweedDestination) : TransactionsUiEvent
    object OnBack : TransactionsUiEvent
}
