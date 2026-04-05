package com.zoewave.probase.seaweed.mobile.bills.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.seaweed.data.FinancialRepository
import com.zoewave.probase.seaweed.data.RecurringExpenseRepository
import com.zoewave.probase.seaweed.model.ExpenseCategory
import com.zoewave.probase.seaweed.model.ExpenseFrequency
import com.zoewave.probase.seaweed.model.RecurringExpense
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class BillsViewModel @Inject constructor(
    private val repository: RecurringExpenseRepository,
    private val financialRepository: FinancialRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<BillsUiState>(BillsUiState.Loading)
    val uiState: StateFlow<BillsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializeDefaultExpenses()
            
            combine(
                repository.getAllExpenses(),
                financialRepository.getMonthlyIncome(),
                financialRepository.getTotalMonthlyFixedCosts()
            ) { expenses, income, totalCosts ->
                BillsUiState.Success(
                    expenses = expenses,
                    monthlyIncome = income,
                    totalFixedCosts = totalCosts
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun onEvent(event: BillsUiEvent) {
        viewModelScope.launch {
            when (event) {
                is BillsUiEvent.UpdateExpenseAmount -> {
                    val expense = (uiState.value as? BillsUiState.Success)?.expenses?.find { it.id == event.id }
                    expense?.let {
                        repository.saveExpense(it.copy(amount = event.amount))
                    }
                }
                is BillsUiEvent.DeleteExpense -> {
                    repository.deleteExpense(event.id)
                }
                is BillsUiEvent.AddExpense -> {
                    val newExpense = RecurringExpense(
                        id = UUID.randomUUID().toString(),
                        name = event.name,
                        amount = event.amount,
                        frequency = event.frequency,
                        category = event.category
                    )
                    repository.saveExpense(newExpense)
                }
            }
        }
    }
}

sealed interface BillsUiState {
    object Loading : BillsUiState
    data class Success(
        val expenses: List<RecurringExpense> = emptyList(),
        val monthlyIncome: Double = 0.0,
        val totalFixedCosts: Double = 0.0
    ) : BillsUiState
}

sealed interface BillsUiEvent {
    data class UpdateExpenseAmount(val id: String, val amount: Double) : BillsUiEvent
    data class DeleteExpense(val id: String) : BillsUiEvent
    data class AddExpense(
        val name: String,
        val amount: Double,
        val frequency: ExpenseFrequency,
        val category: ExpenseCategory
    ) : BillsUiEvent
}
