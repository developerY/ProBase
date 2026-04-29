package com.zoewave.probase.seaweed.wear.features.bills

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.seaweed.data.FinancialRepository
import com.zoewave.probase.seaweed.data.RecurringExpenseRepository
import com.zoewave.probase.seaweed.model.RecurringExpense
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WearBillsViewModel @Inject constructor(
    private val repository: RecurringExpenseRepository,
    private val financialRepository: FinancialRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<WearBillsUiState>(WearBillsUiState.Loading)
    val uiState: StateFlow<WearBillsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                financialRepository.getFinancialProfile(),
                repository.getAllExpenses()
            ) { profile, expenses ->
                val filteredExpenses = expenses.filter { it.averageAmountCents > 0 }.sortedByDescending { it.averageAmountCents }
                WearBillsUiState.Success(
                    expenses = filteredExpenses,
                    totalMonthlyFixedCosts = profile.totalFixedCostsCents.toDouble() / 100.0
                )
            }.onEach { state ->
                _uiState.value = state
            }.launchIn(viewModelScope)
        }
    }

    fun onEvent(event: WearBillsUiEvent) {
        when (event) {
            WearBillsUiEvent.NavigateBack -> { /* Handled in Route */ }
        }
    }
}

sealed interface WearBillsUiState {
    object Loading : WearBillsUiState
    data class Success(
        val expenses: List<RecurringExpense> = emptyList(),
        val totalMonthlyFixedCosts: Double = 0.0
    ) : WearBillsUiState
}

sealed interface WearBillsUiEvent {
    data object NavigateBack : WearBillsUiEvent
}
