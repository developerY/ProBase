package com.zoewave.probase.seaweed.mobile.home.ui

import com.zoewave.probase.seaweed.model.Transaction

sealed interface HomeUiState {
    object Loading : HomeUiState
    data class Success(
        val transactions: List<Transaction> = emptyList(),
        val totalBalance: Double = 0.0
    ) : HomeUiState
}

sealed interface HomeUiEvent {
    data class DeleteTransaction(val id: String) : HomeUiEvent
}
