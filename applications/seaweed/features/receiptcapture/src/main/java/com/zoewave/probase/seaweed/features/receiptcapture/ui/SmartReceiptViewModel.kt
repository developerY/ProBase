package com.zoewave.probase.seaweed.features.receiptcapture.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.core.util.network.NetworkStatsProvider
import com.zoewave.probase.features.ai.capture.data.ImageLoader
import com.zoewave.probase.features.ai.configuration.domain.AiConfigurationSettings
import com.zoewave.probase.features.ai.vision.receipt.ReceiptOrchestrator
import com.zoewave.probase.core.util.CurrencyUtils
import com.zoewave.probase.seaweed.data.TransactionRepository
import com.zoewave.probase.seaweed.model.SpendingType
import com.zoewave.probase.seaweed.model.Transaction
import com.zoewave.probase.seaweed.features.receiptcapture.domain.SmartReceiptDraft
import com.zoewave.probase.seaweed.features.receiptcapture.ui.state.SmartReceiptUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SmartReceiptViewModel @Inject constructor(
    private val orchestrator: ReceiptOrchestrator,
    private val aiSettings: AiConfigurationSettings,
    private val imageLoader: ImageLoader,
    private val networkStatsProvider: NetworkStatsProvider,
    private val transactionRepo: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SmartReceiptUiState>(SmartReceiptUiState.Idle())
    val uiState: StateFlow<SmartReceiptUiState> = _uiState.asStateFlow()

    fun onUserCommentChanged(comment: String) {
        val currentState = _uiState.value
        if (currentState is SmartReceiptUiState.Idle) {
            _uiState.value = currentState.copy(userComment = comment)
        }
    }

    fun setCapturedUri(uri: String) {
        _uiState.value = SmartReceiptUiState.Idle(capturedUri = uri)
    }

    fun analyzeReceipt(uriString: String, userContext: String? = null) {
        viewModelScope.launch(Dispatchers.Default) {
            val netType = networkStatsProvider.getNetworkType()
            _uiState.value = SmartReceiptUiState.Loading(
                logs = listOf("Starting receipt analysis...", "Network: $netType"),
                networkSpeed = netType
            )
            
            val bitmap = imageLoader.loadBitmap(uriString)
            if (bitmap != null) {
                try {
                    val apiKey = aiSettings.getGeminiApiKey()
                    val modelName = aiSettings.aiModelFlow.firstOrNull()
                    val isUsingCloud = !apiKey.isNullOrBlank()
                    
                    _uiState.value = SmartReceiptUiState.Loading(
                        logs = listOf("Image loaded", "Engine: ${if (isUsingCloud) "Cloud ($modelName)" else "Local AI"}"),
                        isUsingCloud = isUsingCloud,
                        networkSpeed = netType
                    )

                    val result = orchestrator.processReceipt(bitmap, apiKey, modelName, userContext)
                    
                    _uiState.value = SmartReceiptUiState.Success(
                        draft = SmartReceiptDraft(
                            merchant = result.merchant,
                            total = result.total,
                            date = result.date,
                            category = result.category,
                            photoUri = uriString
                        ),
                        engineUsed = result.engineUsed,
                        diagnostics = result.logs,
                        warnings = result.warnings
                    )
                } catch (e: Exception) {
                    _uiState.value = SmartReceiptUiState.Error(e.message ?: "Unknown error occurred")
                }
            } else {
                _uiState.value = SmartReceiptUiState.Error("Failed to load captured image.")
            }
        }
    }

    fun saveTransaction(draft: SmartReceiptDraft) {
        viewModelScope.launch {
            val amount = draft.total ?: 0.0
            val transaction = Transaction(
                id = UUID.randomUUID().toString(),
                amountCents = CurrencyUtils.toCents(amount),
                categoryId = draft.category ?: "general_id",
                description = draft.merchant ?: "Unknown Merchant",
                timestamp = System.currentTimeMillis(),
                receiptUri = draft.photoUri,
                defaultType = SpendingType.NEED
            )
            transactionRepo.addTransaction(transaction)
            _uiState.value = SmartReceiptUiState.Idle() // Reset after save
        }
    }

    fun reset() {
        _uiState.value = SmartReceiptUiState.Idle()
    }
}
