package com.zoewave.probase.seaweed.mobile.home.ui

import com.zoewave.probase.seaweed.model.CategoryOverview
import com.zoewave.probase.seaweed.model.Transaction

sealed interface HomeUiState {
    object Loading : HomeUiState
    data class Success(
        val transactions: List<Transaction> = emptyList(),
        val categoriesSummary: List<CategoryOverview> = emptyList(),
        val monthlyIncome: Double = 0.0,
        val totalFixedCosts: Double = 0.0,
        val flexibleMoneyRemaining: Double = 0.0,
        val unallocatedMoney: Double = 0.0,
        val monthProgress: Float = 0f
    ) : HomeUiState
}

sealed interface HomeUiEvent {
    data class DeleteTransaction(val id: String) : HomeUiEvent
    data class DeleteCategory(val category: String) : HomeUiEvent
    data class AddCategory(val name: String) : HomeUiEvent
    data class CombineCategories(val from: String, val to: String) : HomeUiEvent
    object AddRandomTransaction : HomeUiEvent
    object OnBackClicked : HomeUiEvent
}
