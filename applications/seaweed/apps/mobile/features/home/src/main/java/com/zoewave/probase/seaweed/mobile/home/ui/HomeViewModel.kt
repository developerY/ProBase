package com.zoewave.probase.seaweed.mobile.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.seaweed.data.CategoryRepository
import com.zoewave.probase.seaweed.data.FinancialRepository
import com.zoewave.probase.seaweed.data.TransactionRepository
import com.zoewave.probase.seaweed.data.BudgetTargetRepository
import com.zoewave.probase.seaweed.data.TestDataGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val budgetRepository: BudgetTargetRepository,
    private val financialRepository: FinancialRepository,
    private val testDataGenerator: TestDataGenerator
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        financialRepository.getFinancialProfile(),
        repository.getAllTransactions()
    ) { profile, transactions ->
        HomeUiState.Success(
            profile = profile,
            transactions = transactions.sortedByDescending { it.timestamp }.take(10)
        )
    }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState.Loading
        )

    fun onEvent(event: HomeUiEvent) {
        viewModelScope.launch {
            when (event) {
                is HomeUiEvent.DeleteTransaction -> {
                    repository.deleteTransaction(event.id)
                }
                HomeUiEvent.AddRandomTransaction -> {
                    testDataGenerator.generateSingleRandomTransaction()
                }
                is HomeUiEvent.DeleteCategory -> {
                    repository.deleteTransactionsByCategory(event.category)
                    // TODO: handle category entity deletion if needed
                }
                is HomeUiEvent.CombineCategories -> {
                    repository.updateTransactionsCategory(event.from, event.to)
                    budgetRepository.deleteBudget(event.from)
                }
                is HomeUiEvent.AddCategory -> { /* Handle */ }
                HomeUiEvent.OnBackClicked -> { /* Handled in Route */ }
            }
        }
    }
}
