package com.zoewave.probase.seaweed.wear.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.seaweed.data.TransactionRepository
import com.zoewave.probase.seaweed.model.CategoryOverview
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import kotlin.math.absoluteValue

import kotlinx.coroutines.launch
import java.util.UUID
import com.zoewave.probase.seaweed.model.Transaction

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

    fun addRandomTransaction() {
        viewModelScope.launch {
            val categories = listOf("Food", "Transport", "Rent", "Entertainment", "Salary", "Investment")
            val randomTransaction = Transaction(
                id = UUID.randomUUID().toString(),
                amount = (10..10000).random().toDouble() / 100.0 * (if ((0..1).random() == 0) 1 else -1),
                category = categories.random(),
                description = "Watch transaction",
                date = System.currentTimeMillis()
            )
            repository.addTransaction(randomTransaction)
        }
    }
}
