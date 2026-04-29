package com.zoewave.probase.seaweed.wear.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.seaweed.data.FinancialRepository
import com.zoewave.probase.seaweed.data.TransactionRepository
import com.zoewave.probase.seaweed.model.SpendingType
import com.zoewave.probase.seaweed.model.Transaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val financialRepository: FinancialRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = financialRepository.getFinancialProfile()
        .map { profile ->
            HomeUiState.Success(
                totalBalance = profile.flexibleMoneyRemainingCents.toDouble() / 100.0,
                topBudgets = profile.categoryOverviews.filter { it.limitAmountCents != null }.take(3)
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState.Loading
        )

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            HomeUiEvent.AddRandomTransaction -> addRandomTransaction()
            HomeUiEvent.NavigateToTransactions -> { /* Handled in Route */ }
            HomeUiEvent.NavigateToBills -> { /* Handled in Route */ }
        }
    }

    private fun addRandomTransaction() {
        viewModelScope.launch {
            val categories = listOf("Food", "Transport", "Rent", "Entertainment", "Salary", "Investment")
            val amountCents = (10..10000).random().toLong() * (if ((0..1).random() == 0) -1 else 1)
            val randomTransaction = Transaction(
                id = UUID.randomUUID().toString(),
                amountCents = amountCents,
                categoryId = categories.random(),
                description = "Watch transaction",
                timestamp = System.currentTimeMillis(),
                defaultType = if (amountCents < 0) SpendingType.WANT else SpendingType.NEED
            )
            repository.addTransaction(randomTransaction)
        }
    }
}
