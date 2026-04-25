package com.zoewave.probase.seaweed.mobile.bills.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.seaweed.data.FinancialRepository
import com.zoewave.probase.seaweed.data.RecurringExpenseRepository
import com.zoewave.probase.seaweed.model.ExpenseCategory
import com.zoewave.probase.seaweed.model.ExpenseFrequency
import com.zoewave.probase.seaweed.model.RecurringExpense
import com.zoewave.probase.seaweed.model.SpendingType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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
            
            financialRepository.getFinancialProfile()
                .onEach { profile ->
                    val currentState = _uiState.value
                    _uiState.value = BillsUiState.Success(
                        expenses = (currentState as? BillsUiState.Success)?.expenses ?: emptyList(),
                        monthlyIncome = profile.monthlyIncomeCents.toDouble() / 100.0,
                        totalFixedCosts = profile.totalFixedCostsCents.toDouble() / 100.0
                    )
                }
                .launchIn(viewModelScope)

            repository.getAllExpenses()
                .onEach { expenses ->
                    val currentState = _uiState.value
                    if (currentState is BillsUiState.Success) {
                        _uiState.value = currentState.copy(expenses = expenses)
                    } else {
                        _uiState.value = BillsUiState.Success(expenses = expenses)
                    }
                }
                .launchIn(viewModelScope)
        }
    }

    fun onEvent(event: BillsUiEvent) {
        viewModelScope.launch {
            when (event) {
                is BillsUiEvent.UpdateExpenseAmount -> {
                    val expense = (uiState.value as? BillsUiState.Success)?.expenses?.find { it.id == event.id }
                    expense?.let {
                        repository.saveExpense(it.copy(averageAmountCents = (event.amount * 100).toLong()))
                    }
                }
                is BillsUiEvent.UpdateExpenseImportance -> {
                    val expense = (uiState.value as? BillsUiState.Success)?.expenses?.find { it.id == event.id }
                    expense?.let {
                        repository.saveExpense(it.copy(defaultType = event.importance))
                    }
                }
                is BillsUiEvent.DeleteExpense -> {
                    repository.deleteExpense(event.id)
                }
                is BillsUiEvent.AddExpense -> {
                    val newExpense = RecurringExpense(
                        id = UUID.randomUUID().toString(),
                        name = event.name,
                        averageAmountCents = (event.amount * 100).toLong(),
                        frequency = event.frequency,
                        categoryId = "misc_id", // Placeholder
                        defaultType = event.importance
                    )
                    repository.saveExpense(newExpense)
                }
                BillsUiEvent.OnBackClicked -> { /* Handled in Route */ }
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
    data class UpdateExpenseImportance(val id: String, val importance: SpendingType) : BillsUiEvent
    data class DeleteExpense(val id: String) : BillsUiEvent
    data class AddExpense(
        val name: String,
        val amount: Double,
        val frequency: ExpenseFrequency,
        val category: ExpenseCategory,
        val importance: SpendingType = SpendingType.NEED
    ) : BillsUiEvent
    object OnBackClicked : BillsUiEvent
}
