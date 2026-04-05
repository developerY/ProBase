package com.zoewave.probase.seaweed.wear.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.seaweed.data.FinancialRepository
import com.zoewave.probase.seaweed.data.TransactionRepository
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

    val uiState: StateFlow<HomeUiState> = financialRepository.getFlexibleMoneyRemaining()
        .map { flexibleRemaining ->
            HomeUiState.Success(
                totalBalance = flexibleRemaining
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState.Loading
        )

    fun addRandomTransaction() {
        viewModelScope.launch {
            val categories = listOf("Food", "Transport", "Rent", "Entertainment", "Salary", "Investment")
            val randomTransaction = Transaction(
                id = UUID.randomUUID().toString(),
                amount = (10..10000).random().toDouble() / 100.0 * (if ((0..1).random() == 0) -1 else 1), // Mostly spending
                category = categories.random(),
                description = "Watch transaction",
                date = System.currentTimeMillis()
            )
            repository.addTransaction(randomTransaction)
        }
    }
}
