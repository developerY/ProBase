package com.zoewave.probase.seaweed.mobile.transaction.ui

import com.zoewave.probase.seaweed.model.Transaction

sealed interface TransactionsUiState {
    object Loading : TransactionsUiState
    data class Success(
        val transactions: List<Transaction> = emptyList()
    ) : TransactionsUiState
}

sealed interface TransactionsUiEvent {
    data class DeleteTransaction(val id: String) : TransactionsUiEvent
}
