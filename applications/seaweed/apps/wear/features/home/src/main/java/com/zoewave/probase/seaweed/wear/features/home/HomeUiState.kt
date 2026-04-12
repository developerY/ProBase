package com.zoewave.probase.seaweed.wear.features.home

import com.zoewave.probase.seaweed.model.CategoryOverview

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(
        val totalBalance: Double = 0.0,
        val topBudgets: List<CategoryOverview> = emptyList()
    ) : HomeUiState
}

sealed interface HomeUiEvent {
    data object AddRandomTransaction : HomeUiEvent
    data object NavigateToTransactions : HomeUiEvent
    data object NavigateToBills : HomeUiEvent
}
