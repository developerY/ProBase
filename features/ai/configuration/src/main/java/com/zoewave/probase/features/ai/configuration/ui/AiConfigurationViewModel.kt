package com.zoewave.probase.features.ai.configuration.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.core.data.repository.AiConfigurationSettings
import com.zoewave.probase.features.ai.capture.data.SmartCaptureOrchestrator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.zoewave.probase.features.ai.local.data.LocalAiEngine
import com.zoewave.probase.features.ai.local.data.NanoState

@HiltViewModel
class AiConfigurationViewModel @Inject constructor(
    private val settings: AiConfigurationSettings,
    private val orchestrator: SmartCaptureOrchestrator,
    private val localAiEngine: LocalAiEngine
) : ViewModel() {

    private val _isTestingKey = MutableStateFlow(false)
    private val _keyTestResult = MutableStateFlow<String?>(null)
    private val _isTestingModel = MutableStateFlow(false)
    private val _modelTestResult = MutableStateFlow<String?>(null)
    private val _fetchedModels = MutableStateFlow<List<String>?>(null)

    @Suppress("UNCHECKED_CAST")
    private val _isLocalAiAvailable = MutableStateFlow(false)

    init {
        checkLocalAiAvailability()
    }

    private fun checkLocalAiAvailability() {
        viewModelScope.launch {
            val capability = localAiEngine.checkCapability()
            _isLocalAiAvailable.value = capability == NanoState.Available || capability == NanoState.MultimodalAvailable
        }
    }

    @Suppress("UNCHECKED_CAST")
    val uiState: StateFlow<AiConfigurationUiState> = combine(
        settings.isGeminiApiKeySetFlow,
        settings.useByokKey,
        settings.isAiEnabledFlow,
        settings.aiModelFlow,
        settings.useFirebaseVertexAi,
        _isLocalAiAvailable,
        _isTestingKey,
        _keyTestResult,
        _isTestingModel,
        _modelTestResult,
        _fetchedModels
    ) { args: Array<Any?> ->
        AiConfigurationUiState(
            isApiKeySet = args[0] as Boolean,
            useByokKey = args[1] as Boolean,
            isAiEnabled = args[2] as Boolean,
            currentAiModel = args[3] as String,
            useFirebaseVertexAi = args[4] as Boolean,
            isLocalAiAvailable = args[5] as Boolean,
            isTestingKey = args[6] as Boolean,
            keyTestResult = args[7] as String?,
            isTestingModel = args[8] as Boolean,
            modelTestResult = args[9] as String?,
            availableModels = (args[10] as List<String>?) ?: emptyList()
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
            is AiConfigurationEvent.OnUseByokKeyToggled -> {
                viewModelScope.launch { settings.saveUseByokKey(event.enabled) }
            }
            is AiConfigurationEvent.OnUseLocalAiToggled -> {
                viewModelScope.launch { settings.saveUseLocalAi(event.enabled) }
            }
            is AiConfigurationEvent.OnUseFirebaseVertexAiToggled -> {
                viewModelScope.launch { settings.saveUseFirebaseVertexAi(event.enabled) }
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
