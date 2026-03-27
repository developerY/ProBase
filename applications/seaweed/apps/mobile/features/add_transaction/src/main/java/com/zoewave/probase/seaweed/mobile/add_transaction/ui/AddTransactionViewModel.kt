package com.zoewave.probase.seaweed.mobile.add_transaction.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.seaweed.data.TransactionRepository
import com.zoewave.probase.seaweed.model.Transaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class AddTransactionUiState(
    val amount: String = "",
    val category: String = "",
    val description: String = "",
    val isSuccess: Boolean = false
)

sealed interface AddTransactionUiEvent {
    data class AmountChanged(val value: String) : AddTransactionUiEvent
    data class CategoryChanged(val value: String) : AddTransactionUiEvent
    data class DescriptionChanged(val value: String) : AddTransactionUiEvent
    object SaveTransaction : AddTransactionUiEvent
    object BackClicked : AddTransactionUiEvent
}

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    fun onEvent(event: AddTransactionUiEvent) {
        when (event) {
            is AddTransactionUiEvent.AmountChanged -> _uiState.update { it.copy(amount = event.value) }
            is AddTransactionUiEvent.CategoryChanged -> _uiState.update { it.copy(category = event.value) }
            is AddTransactionUiEvent.DescriptionChanged -> _uiState.update { it.copy(description = event.value) }
            AddTransactionUiEvent.SaveTransaction -> saveTransaction()
            AddTransactionUiEvent.BackClicked -> { /* Handled in Route */ }
        }
    }

    private fun saveTransaction() {
        val amountValue = _uiState.value.amount.toDoubleOrNull() ?: 0.0
        val transaction = Transaction(
            id = UUID.randomUUID().toString(),
            amount = amountValue,
            category = _uiState.value.category,
            description = _uiState.value.description,
            date = System.currentTimeMillis()
        )
        viewModelScope.launch {
            repository.addTransaction(transaction)
            _uiState.update { it.copy(isSuccess = true) }
        }
    }
}
