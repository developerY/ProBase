package com.zoewave.probase.seaweed.features.affordability.ui

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.core.model.tasks.SmartTaskDraft
import com.zoewave.probase.features.ai.capture.data.ImageLoader
import com.zoewave.probase.features.ai.capture.data.SmartCaptureOrchestrator
import com.zoewave.probase.features.ai.capture.domain.SmartCaptureSettings
import com.zoewave.probase.features.ai.vision.financial.FinancialAdvisorEngine
import com.zoewave.probase.seaweed.features.spendingcontrol.domain.EnvelopeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import javax.inject.Inject

@HiltViewModel
class AffordabilityViewModel @Inject constructor(
    private val orchestrator: SmartCaptureOrchestrator,
    private val aiSettings: SmartCaptureSettings,
    private val imageLoader: ImageLoader,
    private val envelopeRepo: EnvelopeRepository,
    private val financialAdvisor: FinancialAdvisorEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow<AffordabilityUiState>(AffordabilityUiState.Idle())
    val uiState: StateFlow<AffordabilityUiState> = _uiState.asStateFlow()

    fun setCapturedUri(uri: String) {
        _uiState.value = AffordabilityUiState.Idle(capturedUri = uri)
    }

    fun onUserCommentChanged(comment: String) {
        val currentState = _uiState.value
        if (currentState is AffordabilityUiState.Idle) {
            _uiState.value = currentState.copy(userComment = comment)
        }
    }

    fun analyze(uriString: String?, userComment: String? = null) {
        viewModelScope.launch(Dispatchers.Default) {
            _uiState.value = AffordabilityUiState.Loading(message = "Starting analysis...")
            
            val bitmap = uriString?.let { imageLoader.loadBitmap(it) }
            if (bitmap == null && userComment.isNullOrBlank()) {
                _uiState.value = AffordabilityUiState.Error("No content to analyze.")
                return@launch
            }

            try {
                val apiKey = aiSettings.userApiKeyFlow.firstOrNull()
                val modelName = aiSettings.userAiModelFlow.first()
                
                _uiState.value = AffordabilityUiState.Loading(
                    message = "Extracting item details...",
                    logs = listOf("Orchestrating SmartCapture extraction")
                )

                // 1. Extract Item Details using SmartCapture Orchestrator
                val extractionResult = orchestrator.processImage(
                    bitmap = bitmap,
                    apiKey = apiKey,
                    modelName = modelName,
                    userContext = userComment,
                    onLog = { log ->
                        updateLogs(log)
                    }
                )

                val error = extractionResult.error
                if (error != null) {
                    _uiState.value = AffordabilityUiState.Error(error, extractionResult.logs)
                    return@launch
                }

                val draft = extractionResult.draft
                
                // 2. Get Seaweed Financial Context
                _uiState.update { 
                    if (it is AffordabilityUiState.Loading) it.copy(message = "Checking envelopes...") else it 
                }
                
                val envelopes = envelopeRepo.getAllEnvelopes().firstOrNull() ?: emptyList()
                val jsonContext = buildJsonObject {
                    put("currency", "USD")
                    putJsonObject("envelopes") {
                        envelopes.forEach { envelope ->
                            put(envelope.name.lowercase(), (envelope.monthlyLimitCents - envelope.currentSpentCents) / 100.0)
                        }
                    }
                }.toString()

                // 3. Get Financial Advice if we have an API Key
                val advice = if (!apiKey.isNullOrBlank() && bitmap != null) {
                    _uiState.update { 
                        if (it is AffordabilityUiState.Loading) it.copy(message = "Consulting Gemini...") else it 
                    }
                    financialAdvisor.analyzeFinancialImpact(
                        bitmap = bitmap,
                        apiKey = apiKey,
                        modelName = modelName,
                        financialContext = jsonContext,
                        userContext = "Item: ${draft.taskName}, Extracted Price: ${draft.budget ?: "unknown"}. User note: $userComment",
                        deviceBranding = "Seaweed App"
                    )
                } else {
                    "Cloud AI advice unavailable. Based on your envelopes, please check manually if ${draft.budget ?: "this item"} fits."
                }

                _uiState.value = AffordabilityUiState.Success(
                    draft = draft,
                    affordabilityAdvice = advice,
                    engineUsed = extractionResult.engineUsed,
                    diagnostics = extractionResult.logs
                )

            } catch (e: Exception) {
                _uiState.value = AffordabilityUiState.Error(e.message ?: "Analysis failed.")
            }
        }
    }

    private fun updateLogs(newLog: String) {
        _uiState.update { state ->
            if (state is AffordabilityUiState.Loading) {
                state.copy(logs = state.logs + newLog)
            } else state
        }
    }

    fun reset() {
        _uiState.value = AffordabilityUiState.Idle()
    }
}
