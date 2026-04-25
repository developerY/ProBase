package com.zoewave.probase.seaweed.mobile.transaction.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.graphics.Bitmap
import android.util.Log
import com.zoewave.probase.core.util.CurrencyUtils
import com.zoewave.probase.features.ai.capture.data.ImageLoader
import com.zoewave.probase.features.ai.configuration.domain.AiConfigurationSettings
import com.zoewave.probase.features.ai.vision.receipt.ReceiptOrchestrator
import com.zoewave.probase.seaweed.data.BudgetTargetRepository
import com.zoewave.probase.seaweed.data.TransactionRepository
import com.zoewave.probase.seaweed.model.SpendingType
import com.zoewave.probase.seaweed.model.Transaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

data class AddTransactionUiState(
    val amount: String = "",
    val category: String = "",
    val description: String = "",
    val importance: SpendingType = SpendingType.NEED,
    val receiptUri: String? = null,
    val isSuccess: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isTipWidgetVisible: Boolean = false,
    val tipPercentage: Int? = null,
    val customTipAmount: String = "",
    val isSplitWidgetVisible: Boolean = false,
    val splitCount: Int = 1,
    val recentCategories: List<String> = emptyList(),
    val isCategorySuggestionsVisible: Boolean = false,
    val showCaptureTypeSelection: Boolean = false,
    val userContextComment: String = "",
    val lastAiDebugInfo: AiDebugInfo? = null
)

@Serializable
data class AiDebugInfo(
    val rawResponse: String,
    val logs: List<String>,
    val engineUsed: String,
    val whatIsThis: String? = null
)

sealed interface AddTransactionUiEvent {
    data class AmountChanged(val value: String) : AddTransactionUiEvent
    data class CategoryChanged(val value: String) : AddTransactionUiEvent
    data class ImportanceChanged(val value: SpendingType) : AddTransactionUiEvent
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
    data class SetCategorySuggestionsVisible(val visible: Boolean) : AddTransactionUiEvent
    data class AdjustAmount(val delta: Double) : AddTransactionUiEvent
    data class ReceiptImageCaptured(val bitmap: Bitmap) : AddTransactionUiEvent
    object SelectReceiptMode : AddTransactionUiEvent
    data class SelectPurchaseMode(val comment: String) : AddTransactionUiEvent
    data class UserCommentChanged(val comment: String) : AddTransactionUiEvent
    object CancelCaptureSelection : AddTransactionUiEvent
    object DebugAiClicked : AddTransactionUiEvent
}

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val budgetRepository: BudgetTargetRepository,
    private val imageLoader: ImageLoader,
    private val orchestrator: ReceiptOrchestrator,
    private val aiSettings: AiConfigurationSettings
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    
    val uiState: StateFlow<AddTransactionUiState> = combine(
        _uiState,
        repository.getAllTransactions(),
        budgetRepository.getAllBudgets()
    ) { state, transactions, budgets ->
        val transactionCategories = transactions.map { it.categoryId }.distinct()
        val budgetCategories = budgets.map { it.categoryName }
        val allCategories = (transactionCategories + budgetCategories).distinct().sorted()
        state.copy(recentCategories = allCategories)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AddTransactionUiState()
    )

    fun onEvent(event: AddTransactionUiEvent) {
        when (event) {
            is AddTransactionUiEvent.AmountChanged -> _uiState.update { it.copy(amount = event.value, errorMessage = null) }
            is AddTransactionUiEvent.CategoryChanged -> _uiState.update { it.copy(category = event.value, errorMessage = null) }
            is AddTransactionUiEvent.ImportanceChanged -> _uiState.update { it.copy(importance = event.value) }
            is AddTransactionUiEvent.DescriptionChanged -> _uiState.update { it.copy(description = event.value, errorMessage = null) }
            is AddTransactionUiEvent.ReceiptAttached -> {
                _uiState.update { it.copy(receiptUri = event.uri, showCaptureTypeSelection = true, errorMessage = null) }
            }
            AddTransactionUiEvent.SelectReceiptMode -> {
                val uri = _uiState.value.receiptUri
                _uiState.update { it.copy(showCaptureTypeSelection = false) }
                if (uri != null) processReceiptUri(uri, null)
            }
            is AddTransactionUiEvent.SelectPurchaseMode -> {
                val uri = _uiState.value.receiptUri
                _uiState.update { it.copy(showCaptureTypeSelection = false) }
                if (uri != null) processReceiptUri(uri, event.comment)
            }
            is AddTransactionUiEvent.UserCommentChanged -> {
                _uiState.update { it.copy(userContextComment = event.comment) }
            }
            AddTransactionUiEvent.CancelCaptureSelection -> {
                _uiState.update { it.copy(showCaptureTypeSelection = false, receiptUri = null) }
            }
            AddTransactionUiEvent.DebugAiClicked -> { /* Handled in Route */ }
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
            is AddTransactionUiEvent.SetCategorySuggestionsVisible -> _uiState.update { it.copy(isCategorySuggestionsVisible = event.visible) }
            is AddTransactionUiEvent.AdjustAmount -> {
                _uiState.update { state ->
                    val currentAmount = state.amount.toDoubleOrNull() ?: 0.0
                    val newAmount = (currentAmount + event.delta).coerceAtLeast(0.0)
                    state.copy(amount = if (newAmount > 0) String.format(Locale.getDefault(), "%.2f", newAmount) else "")
                }
            }
            is AddTransactionUiEvent.ReceiptImageCaptured -> processReceiptImage(event.bitmap)
        }
    }

    private fun processReceiptImage(bitmap: Bitmap, userComment: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val apiKey = aiSettings.getGeminiApiKey()
                val modelName = aiSettings.aiModelFlow.firstOrNull()
                
                Log.d("AddTransactionVM", "Sending to Orchestrator (multimodal)...")
                val result = orchestrator.processReceipt(bitmap, apiKey, modelName, userComment)
                
                Log.d("AddTransactionVM", "AI Result: $result")
                _uiState.update { 
                    val updated = it.copy(
                        amount = if ((result.total ?: 0.0) > 0) String.format(Locale.getDefault(), "%.2f", result.total) else it.amount,
                        category = result.category ?: it.category,
                        description = result.merchant ?: it.description,
                        isLoading = false,
                        errorMessage = null,
                        lastAiDebugInfo = AiDebugInfo(
                            rawResponse = result.rawResponse ?: "No raw data from engine",
                            logs = result.logs,
                            engineUsed = result.engineUsed,
                            whatIsThis = result.whatIsThis
                        )
                    )
                    Log.d("AddTransactionVM", "Updating UI State: description=${updated.description}, amount=${updated.amount}, category=${updated.category}")
                    updated
                }
            } catch (e: Exception) {
                Log.e("AddTransactionVM", "AI Analysis failed", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to parse receipt: ${e.message}") }
            }
        }
    }

    private fun processReceiptUri(uri: String, userComment: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val bitmap = imageLoader.loadBitmap(uri)
            if (bitmap != null) {
                processReceiptImage(bitmap, userComment)
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to load image for AI analysis") }
            }
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
                amountCents = CurrencyUtils.toCents(totalAmount),
                categoryId = _uiState.value.category, // Assuming category is the ID here for now
                description = _uiState.value.description,
                timestamp = System.currentTimeMillis(),
                receiptUri = _uiState.value.receiptUri,
                defaultType = _uiState.value.importance
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
