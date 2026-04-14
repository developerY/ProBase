package com.zoewave.probase.features.ai.configuration.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.features.ai.configuration.domain.AiConfigurationSettings
import com.zoewave.probase.features.smartcapture.data.SmartCaptureOrchestrator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AiConfigurationViewModel @Inject constructor(
    private val settings: AiConfigurationSettings,
    private val orchestrator: SmartCaptureOrchestrator
) : ViewModel() {

    private val _isTestingKey = MutableStateFlow(false)
    private val _keyTestResult = MutableStateFlow<String?>(null)
    private val _isTestingModel = MutableStateFlow(false)
    private val _modelTestResult = MutableStateFlow<String?>(null)
    private val _fetchedModels = MutableStateFlow<List<String>?>(null)

    @Suppress("UNCHECKED_CAST")
    val uiState: StateFlow<AiConfigurationUiState> = combine(
        settings.isGeminiApiKeySetFlow,
        settings.isAiEnabledFlow,
        settings.aiModelFlow,
        _isTestingKey,
        _keyTestResult,
        _isTestingModel,
        _modelTestResult,
        _fetchedModels
    ) { args: Array<Any?> ->
        AiConfigurationUiState(
            isApiKeySet = args[0] as Boolean,
            isAiEnabled = args[1] as Boolean,
            currentAiModel = args[2] as String,
            isTestingKey = args[3] as Boolean,
            keyTestResult = args[4] as String?,
            isTestingModel = args[5] as Boolean,
            modelTestResult = args[6] as String?,
            availableModels = (args[7] as List<String>?) ?: emptyList()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AiConfigurationUiState()
    )

    fun onEvent(event: AiConfigurationEvent) {
        when (event) {
            is AiConfigurationEvent.OnAiEnabledToggled -> {
                viewModelScope.launch { settings.saveAiEnabled(event.enabled) }
            }
            is AiConfigurationEvent.OnGeminiApiKeyChanged -> {
                viewModelScope.launch { settings.saveGeminiApiKey(event.apiKey) }
            }
            is AiConfigurationEvent.OnAiModelSelected -> {
                viewModelScope.launch { settings.saveAiModel(event.model) }
            }
            is AiConfigurationEvent.OnTestApiKeyClicked -> {
                testApiKey()
            }
            is AiConfigurationEvent.OnTestModelClicked -> {
                testModel()
            }
        }
    }

    private fun testApiKey() {
        viewModelScope.launch {
            _isTestingKey.value = true
            _keyTestResult.value = "Testing connection..."
            
            val key = settings.getGeminiApiKey()
            
            if (key.isNullOrBlank()) {
                _keyTestResult.value = "Error: No API key saved."
                _isTestingKey.value = false
                return@launch
            }

            val result = orchestrator.validateApiKey(key)
            _keyTestResult.value = result.first
            if (result.second.isNotEmpty()) {
                _fetchedModels.value = result.second
            }
            _isTestingKey.value = false
        }
    }

    private fun testModel() {
        viewModelScope.launch {
            val key = settings.getGeminiApiKey()
            val model = settings.aiModelFlow.firstOrNull() ?: "gemini-1.5-flash"

            if (key.isNullOrBlank()) {
                _keyTestResult.value = "Error: No API key saved."
                return@launch
            }

            _isTestingModel.value = true
            _modelTestResult.value = "Pinging model..."

            val result = orchestrator.testModel(key, model)
            _modelTestResult.value = result
            _isTestingModel.value = false
        }
    }
}
