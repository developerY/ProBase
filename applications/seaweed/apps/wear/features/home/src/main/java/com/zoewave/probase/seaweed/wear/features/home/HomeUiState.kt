package com.zoewave.probase.seaweed.wear.features.home

import com.zoewave.probase.seaweed.model.FinancialProfile
import com.zoewave.probase.seaweed.model.Transaction

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(
        val profile: FinancialProfile,
        val recentTransactions: List<Transaction>
    ) : HomeUiState
}

sealed interface HomeUiEvent {
    data object NavigateToTransactions : HomeUiEvent
    data object NavigateToBills : HomeUiEvent
}
