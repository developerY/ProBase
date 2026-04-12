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
            financialRepository.getFinancialProfile()
                .onEach { profile ->
                    _uiState.value = WearBillsUiState.Success(
                        expenses = (uiState.value as? WearBillsUiState.Success)?.expenses ?: emptyList(),
                        totalMonthlyFixedCosts = profile.totalFixedCosts
                    )
                }
                .launchIn(viewModelScope)

            repository.getAllExpenses()
                .onEach { expenses ->
                    val currentState = _uiState.value
                    val filteredExpenses = expenses.filter { it.amount > 0 }.sortedByDescending { it.amount }
                    if (currentState is WearBillsUiState.Success) {
                        _uiState.value = currentState.copy(expenses = filteredExpenses)
                    } else {
                        _uiState.value = WearBillsUiState.Success(expenses = filteredExpenses)
                    }
                }
                .launchIn(viewModelScope)
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
