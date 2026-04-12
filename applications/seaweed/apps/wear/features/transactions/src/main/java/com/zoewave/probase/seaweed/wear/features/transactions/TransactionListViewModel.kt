package com.zoewave.probase.seaweed.wear.features.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.seaweed.data.TransactionRepository
import com.zoewave.probase.seaweed.model.Transaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class TransactionListViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    val uiState: StateFlow<TransactionListUiState> = repository.getAllTransactions()
        .map { transactions ->
            TransactionListUiState.Success(
                transactions = transactions.sortedByDescending { it.date }
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TransactionListUiState.Loading
        )

    fun onEvent(event: TransactionListUiEvent) {
        when (event) {
            TransactionListUiEvent.NavigateBack -> { /* Handled in Route */ }
        }
    }
}

sealed interface TransactionListUiState {
    data object Loading : TransactionListUiState
    data class Success(
        val transactions: List<Transaction> = emptyList()
    ) : TransactionListUiState
}

sealed interface TransactionListUiEvent {
    data object NavigateBack : TransactionListUiEvent
}
