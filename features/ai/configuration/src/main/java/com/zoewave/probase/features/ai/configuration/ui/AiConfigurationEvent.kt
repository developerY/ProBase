package com.zoewave.probase.features.ai.configuration.ui

sealed interface AiConfigurationEvent {
    data class OnAiEnabledToggled(val enabled: Boolean) : AiConfigurationEvent
    data class OnGeminiApiKeyChanged(val apiKey: String?) : AiConfigurationEvent
    data class OnAiModelSelected(val model: String) : AiConfigurationEvent
    data object OnTestApiKeyClicked : AiConfigurationEvent
    data object OnTestModelClicked : AiConfigurationEvent
}
