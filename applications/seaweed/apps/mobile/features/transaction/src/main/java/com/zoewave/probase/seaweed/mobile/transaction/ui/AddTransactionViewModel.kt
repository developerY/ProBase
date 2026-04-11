package com.zoewave.probase.seaweed.mobile.transaction.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.seaweed.data.TransactionRepository
import com.zoewave.probase.seaweed.model.Transaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

data class AddTransactionUiState(
    val amount: String = "",
    val category: String = "",
    val description: String = "",
    val receiptUri: String? = null,
    val isSuccess: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isTipWidgetVisible: Boolean = false,
    val tipPercentage: Int? = null,
    val customTipAmount: String = "",
    val isSplitWidgetVisible: Boolean = false,
    val splitCount: Int = 1,
    val recentCategories: List<String> = emptyList()
)

sealed interface AddTransactionUiEvent {
    data class AmountChanged(val value: String) : AddTransactionUiEvent
    data class CategoryChanged(val value: String) : AddTransactionUiEvent
    data class DescriptionChanged(val value: String) : AddTransactionUiEvent
    data class ReceiptAttached(val uri: String) : AddTransactionUiEvent
    object SaveTransaction : AddTransactionUiEvent
    object BackClicked : AddTransactionUiEvent
    object SuccessConsumed : AddTransactionUiEvent
    object ToggleTipWidget : AddTransactionUiEvent
    data class SelectTipPercentage(val percentage: Int?) : AddTransactionUiEvent
    data class CustomTipAmountChanged(val value: String) : AddTransactionUiEvent
    object ToggleSplitWidget : AddTransactionUiEvent
    data class SplitCountChanged(val count: Int) : AddTransactionUiEvent
}

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    
    val uiState: StateFlow<AddTransactionUiState> = combine(
        _uiState,
        repository.getAllTransactions().map { transactions ->
            transactions.map { it.category }.distinct().take(10)
        }
    ) { state, recent ->
        state.copy(recentCategories = recent)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AddTransactionUiState()
    )

    fun onEvent(event: AddTransactionUiEvent) {
        when (event) {
            is AddTransactionUiEvent.AmountChanged -> _uiState.update { it.copy(amount = event.value, errorMessage = null) }
            is AddTransactionUiEvent.CategoryChanged -> _uiState.update { it.copy(category = event.value, errorMessage = null) }
            is AddTransactionUiEvent.DescriptionChanged -> _uiState.update { it.copy(description = event.value, errorMessage = null) }
            is AddTransactionUiEvent.ReceiptAttached -> _uiState.update { it.copy(receiptUri = event.uri, errorMessage = null) }
            AddTransactionUiEvent.SaveTransaction -> saveTransaction()
            AddTransactionUiEvent.BackClicked -> { /* Handled in Route */ }
            AddTransactionUiEvent.SuccessConsumed -> _uiState.update { it.copy(isSuccess = false) }
            AddTransactionUiEvent.ToggleTipWidget -> _uiState.update { it.copy(isTipWidgetVisible = !it.isTipWidgetVisible) }
            is AddTransactionUiEvent.SelectTipPercentage -> {
                _uiState.update { state ->
                    val baseAmount = state.amount.toDoubleOrNull() ?: 0.0
                    val tipAmount = if (event.percentage != null) {
                        baseAmount * (event.percentage / 100.0)
                    } else {
                        0.0
                    }
                    state.copy(
                        tipPercentage = event.percentage,
                        customTipAmount = if (event.percentage != null) String.format(Locale.getDefault(), "%.2f", tipAmount) else ""
                    )
                }
            }
            is AddTransactionUiEvent.CustomTipAmountChanged -> {
                _uiState.update { it.copy(customTipAmount = event.value, tipPercentage = null) }
            }
            AddTransactionUiEvent.ToggleSplitWidget -> _uiState.update { it.copy(isSplitWidgetVisible = !it.isSplitWidgetVisible) }
            is AddTransactionUiEvent.SplitCountChanged -> _uiState.update { it.copy(splitCount = event.count.coerceAtLeast(1)) }
        }
    }

    private fun saveTransaction() {
        val amountValue = _uiState.value.amount.toDoubleOrNull() ?: 0.0
        val tipValue = _uiState.value.customTipAmount.toDoubleOrNull() ?: 0.0
        val totalAmount = amountValue + tipValue

        if (totalAmount <= 0.0 || _uiState.value.category.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid amount and category") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val transaction = Transaction(
                id = UUID.randomUUID().toString(),
                amount = totalAmount,
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
