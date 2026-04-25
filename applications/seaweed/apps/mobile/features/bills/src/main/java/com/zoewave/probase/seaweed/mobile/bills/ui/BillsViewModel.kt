package com.zoewave.probase.seaweed.mobile.bills.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.seaweed.data.CategoryRepository
import com.zoewave.probase.seaweed.data.FinancialRepository
import com.zoewave.probase.seaweed.data.RecurringExpenseRepository
import com.zoewave.probase.seaweed.model.Category
import com.zoewave.probase.seaweed.model.ExpenseCategory
import com.zoewave.probase.seaweed.model.ExpenseFrequency
import com.zoewave.probase.seaweed.model.RecurringExpense
import com.zoewave.probase.seaweed.model.SpendingType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class BillsViewModel @Inject constructor(
    private val repository: RecurringExpenseRepository,
    private val financialRepository: FinancialRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    val uiState: StateFlow<BillsUiState> = combine(
        repository.getAllExpenses(),
        financialRepository.getFinancialProfile(),
        categoryRepository.getAllCategories()
    ) { expenses, profile, categories ->
        BillsUiState.Success(
            expenses = expenses,
            monthlyIncome = profile.monthlyIncomeCents.toDouble() / 100.0,
            totalFixedCosts = profile.totalFixedCostsCents.toDouble() / 100.0,
            categoryMap = categories.associate { it.id to it.name }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = BillsUiState.Loading
    )

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
        val totalFixedCosts: Double = 0.0,
        val categoryMap: Map<String, String> = emptyMap()
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
