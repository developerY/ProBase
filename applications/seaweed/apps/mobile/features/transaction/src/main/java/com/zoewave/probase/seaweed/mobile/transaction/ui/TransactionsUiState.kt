package com.zoewave.probase.seaweed.mobile.transaction.ui

import com.zoewave.probase.seaweed.model.Transaction

sealed interface TransactionsUiState {
    object Loading : TransactionsUiState
    data class Success(
        val transactions: List<Transaction> = emptyList(),
        val filteredTransactions: List<Transaction> = emptyList(),
        val categories: List<String> = emptyList(),
        val selectedCategory: String? = null,
        val selectedTransactionId: String? = null,
        val selectedTransaction: Transaction? = null
    ) : TransactionsUiState
}

sealed interface TransactionsUiEvent {
    data class DeleteTransaction(val id: String) : TransactionsUiEvent
    data class SelectCategory(val category: String?) : TransactionsUiEvent
    data class SelectTransaction(val id: String?) : TransactionsUiEvent
}
