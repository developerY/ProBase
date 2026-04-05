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
                repository.getAllExpenses(),
                financialRepository.getTotalMonthlyFixedCosts()
            ) { expenses, totalCosts ->
                WearBillsUiState.Success(
                    expenses = expenses.filter { it.amount > 0 }.sortedByDescending { it.amount },
                    totalMonthlyFixedCosts = totalCosts
                )
            }.collect { state ->
                _uiState.value = state
            }
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
