package com.zoewave.probase.seaweed.wear.features.home

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(
        val totalBalance: Double = 0.0
    ) : HomeUiState
}
