package com.zoewave.probase.seaweed.mobile.home.ui

import com.zoewave.probase.seaweed.model.FinancialProfile
import com.zoewave.probase.seaweed.model.Transaction
import com.zoewave.probase.seaweed.model.SpendingType

sealed interface HomeUiState {
    object Loading : HomeUiState
    data class Success(
        val profile: FinancialProfile,
        val transactions: List<Transaction> = emptyList()
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
