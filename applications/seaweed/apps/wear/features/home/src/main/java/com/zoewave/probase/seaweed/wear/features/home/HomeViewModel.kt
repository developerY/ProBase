package com.zoewave.probase.seaweed.wear.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.seaweed.data.FinancialRepository
import com.zoewave.probase.seaweed.data.TransactionRepository
import com.zoewave.probase.seaweed.model.Transaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val financialRepository: FinancialRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        financialRepository.getFinancialProfile(),
        transactionRepository.getAllTransactions()
    ) { profile, transactions ->
        HomeUiState.Success(
            profile = profile,
            recentTransactions = transactions.sortedByDescending { it.timestamp }.take(5)
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState.Loading
        )

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            HomeUiEvent.NavigateToTransactions -> { /* Handled in Route */ }
            HomeUiEvent.NavigateToBills -> { /* Handled in Route */ }
        }
    }
}
