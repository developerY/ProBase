package com.zoewave.probase.seaweed.mobile.transaction.ui

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
import java.util.UUID
import javax.inject.Inject

data class AddTransactionUiState(
    val amount: String = "",
    val category: String = "",
    val description: String = "",
    val receiptUri: String? = null,
    val isSuccess: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed interface AddTransactionUiEvent {
    data class AmountChanged(val value: String) : AddTransactionUiEvent
    data class CategoryChanged(val value: String) : AddTransactionUiEvent
    data class DescriptionChanged(val value: String) : AddTransactionUiEvent
    data class ReceiptAttached(val uri: String) : AddTransactionUiEvent
    object SaveTransaction : AddTransactionUiEvent
    object BackClicked : AddTransactionUiEvent
    object SuccessConsumed : AddTransactionUiEvent
}

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    fun onEvent(event: AddTransactionUiEvent) {
        when (event) {
            is AddTransactionUiEvent.AmountChanged -> _uiState.update { it.copy(amount = event.value, errorMessage = null) }
            is AddTransactionUiEvent.CategoryChanged -> _uiState.update { it.copy(category = event.value, errorMessage = null) }
            is AddTransactionUiEvent.DescriptionChanged -> _uiState.update { it.copy(description = event.value, errorMessage = null) }
            is AddTransactionUiEvent.ReceiptAttached -> _uiState.update { it.copy(receiptUri = event.uri, errorMessage = null) }
            AddTransactionUiEvent.SaveTransaction -> saveTransaction()
            AddTransactionUiEvent.BackClicked -> { /* Handled in Route */ }
            AddTransactionUiEvent.SuccessConsumed -> _uiState.update { it.copy(isSuccess = false) }
        }
    }

    private fun saveTransaction() {
        val amountValue = _uiState.value.amount.toDoubleOrNull() ?: 0.0
        if (amountValue <= 0.0 || _uiState.value.category.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid amount and category") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val transaction = Transaction(
                id = UUID.randomUUID().toString(),
                amount = amountValue,
                category = _uiState.value.category,
                description = _uiState.value.description,
                date = System.currentTimeMillis(),
                receiptUri = _uiState.value.receiptUri
            )
            try {
                repository.addTransaction(transaction)
                _uiState.update { it.copy(isSuccess = true, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ) }
            }
        }
    }
}
