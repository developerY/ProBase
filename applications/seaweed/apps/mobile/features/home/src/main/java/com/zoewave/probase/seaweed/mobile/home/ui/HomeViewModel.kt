package com.zoewave.probase.seaweed.mobile.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.seaweed.data.TransactionRepository
import com.zoewave.probase.seaweed.model.CategoryOverview
import com.zoewave.probase.seaweed.model.Transaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import kotlin.math.absoluteValue

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = repository.getAllTransactions()
        .map { transactions ->
            val categoriesSummary = transactions.filter { it.amount < 0 }
                .groupBy { it.category }
                .map { (category, categoryTransactions) ->
                    CategoryOverview(
                        name = category,
                        totalAmount = categoryTransactions.sumOf { it.amount }.absoluteValue,
                        transactionCount = categoryTransactions.size
                    )
                }
                .sortedByDescending { it.totalAmount }

            HomeUiState.Success(
                transactions = transactions,
                categoriesSummary = categoriesSummary,
                totalBalance = transactions.sumOf { it.amount }
            )
        }
        .stateIn(
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
                    val categories = listOf("Food", "Transport", "Rent", "Entertainment", "Salary", "Investment")
                    val randomTransaction = Transaction(
                        id = UUID.randomUUID().toString(),
                        amount = (10..10000).random().toDouble() / 100.0 * (if ((0..1).random() == 0) 1 else -1),
                        category = categories.random(),
                        description = "Random transaction",
                        date = System.currentTimeMillis() - (0..30L * 24 * 60 * 60 * 1000).random()
                    )
                    repository.addTransaction(randomTransaction)
                }
            }
        }
    }
}
