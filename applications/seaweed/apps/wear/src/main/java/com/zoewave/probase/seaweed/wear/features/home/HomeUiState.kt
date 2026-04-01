package com.zoewave.probase.seaweed.wear.features.home

import com.zoewave.probase.seaweed.model.CategoryOverview
import com.zoewave.probase.seaweed.model.Transaction

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(
        val transactions: List<Transaction> = emptyList(),
        val categoriesSummary: List<CategoryOverview> = emptyList(),
        val totalBalance: Double = 0.0
    ) : HomeUiState
}
