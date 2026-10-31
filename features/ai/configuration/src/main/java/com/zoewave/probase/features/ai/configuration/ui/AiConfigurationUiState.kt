package com.zoewave.probase.features.ai.configuration.ui

import com.zoewave.probase.features.ai.local.data.LocalModelState

data class AiConfigurationUiState(
    val isApiKeySet: Boolean = false,
    val isAiEnabled: Boolean = true,
    val currentAiModel: String = "gemini-2.0-flash",
    val availableModels: List<String> = emptyList(),
    val isTestingKey: Boolean = false,
    val keyTestResult: String? = null,
    val isTestingModel: Boolean = false,
    val modelTestResult: String? = null,
    val useByokKey: Boolean = true,
    val useLocalAi: Boolean = true,
    val useFirebaseVertexAi: Boolean = true,
    val useDownloadAi: Boolean = false,
    val isLocalAiAvailable: Boolean = true,
    val localModelState: LocalModelState = LocalModelState.NotDownloaded
)
