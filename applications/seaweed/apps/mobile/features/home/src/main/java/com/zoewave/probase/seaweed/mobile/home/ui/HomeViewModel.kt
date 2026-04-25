package com.zoewave.probase.seaweed.mobile.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.seaweed.data.CategoryRepository
import com.zoewave.probase.seaweed.data.FinancialRepository
import com.zoewave.probase.seaweed.data.TransactionRepository
import com.zoewave.probase.seaweed.data.BudgetTargetRepository
import com.zoewave.probase.seaweed.data.TestDataGenerator
import com.zoewave.probase.seaweed.model.BudgetTarget
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
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
            transactions = transactions.sortedByDescending { it.timestamp }.take(10),
            categoriesSummary = profile.categoryOverviews,
            monthlyIncome = profile.monthlyIncome,
            totalFixedCosts = profile.totalFixedCosts,
            flexibleMoneyRemaining = profile.flexibleMoneyRemaining,
            unallocatedMoney = profile.unallocatedMoney,
            monthProgress = profile.monthProgress
        )
    }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState.Loading
        )

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.DeleteTransaction -> {
                viewModelScope.launch {
                    repository.deleteTransaction(event.id)
                }
            }
            HomeUiEvent.AddRandomTransaction -> {
                viewModelScope.launch {
                    testDataGenerator.generateSingleRandomTransaction()
                }
            }
            is HomeUiEvent.DeleteCategory -> {
                viewModelScope.launch {
                    repository.deleteTransactionsByCategory(event.category)
                    // TODO: also delete category if we want to remove from 'brain'
                }
            }
            is HomeUiEvent.AddCategory -> {
                viewModelScope.launch {
                    // Default to WANT for custom categories for now? or NEED?
                    // Category entity needs to be handled here too
                }
            }
            is HomeUiEvent.CombineCategories -> {
                viewModelScope.launch {
                    repository.updateTransactionsCategory(event.from, event.to)
                    budgetRepository.deleteBudget(event.from)
                }
            }
            HomeUiEvent.OnBackClicked -> { /* Handled in Route */ }
        }
    }
}
