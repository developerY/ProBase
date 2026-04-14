package com.zoewave.probase.features.ai.configuration.ui

data class AiConfigurationUiState(
    val isApiKeySet: Boolean = false,
    val isAiEnabled: Boolean = false,
    val currentAiModel: String = "gemini-1.5-flash",
    val availableModels: List<String> = emptyList(),
    val isTestingKey: Boolean = false,
    val keyTestResult: String? = null,
    val isTestingModel: Boolean = false,
    val modelTestResult: String? = null
)
