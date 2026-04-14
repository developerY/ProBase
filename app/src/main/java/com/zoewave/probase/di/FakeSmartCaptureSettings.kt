package com.zoewave.probase.di

import com.zoewave.probase.features.ai.capture.domain.SmartCaptureSettings
import com.zoewave.probase.features.ai.configuration.domain.AiConfigurationSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeSmartCaptureSettings @Inject constructor() : SmartCaptureSettings, AiConfigurationSettings {
    override val userApiKeyFlow: Flow<String?> = flowOf(null)
    override val userAiModelFlow: Flow<String> = flowOf("gemini-1.5-flash")

    override val isAiEnabledFlow: Flow<Boolean> = flowOf(true)
    override suspend fun saveAiEnabled(enabled: Boolean) {}

    override val aiModelFlow: Flow<String> = flowOf("gemini-1.5-flash")
    override suspend fun saveAiModel(model: String) {}

    override fun getGeminiApiKey(): String? = null
    override val isGeminiApiKeySetFlow: Flow<Boolean> = flowOf(false)
    override suspend fun saveGeminiApiKey(apiKey: String?) {}
}
