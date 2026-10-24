package com.zoewave.probase.goswift.data

import com.zoewave.probase.core.data.repository.AiConfigurationSettings
import com.zoewave.probase.features.ai.capture.domain.SmartCaptureSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoSwiftAiSettings @Inject constructor() : AiConfigurationSettings, SmartCaptureSettings {
    private val _apiKey = MutableStateFlow<String?>(null)
    private val _aiModel = MutableStateFlow("gemini-1.5-flash")
    private val _useFirebase = MutableStateFlow(true)
    private val _useByokKey = MutableStateFlow(false)
    private val _useLocalAi = MutableStateFlow(false)
    private val _aiEnabled = MutableStateFlow(true)

    override val userApiKeyFlow: Flow<String?> = _apiKey.asStateFlow()
    override val userAiModelFlow: Flow<String> = _aiModel.asStateFlow()

    override val isAiEnabledFlow: Flow<Boolean> = _aiEnabled.asStateFlow()
    override suspend fun saveAiEnabled(enabled: Boolean) {
        _aiEnabled.value = enabled
    }

    override val aiModelFlow: Flow<String> = _aiModel.asStateFlow()
    override suspend fun saveAiModel(model: String) {
        _aiModel.value = model
    }

    override fun getGeminiApiKey(): String? = _apiKey.value
    override val isGeminiApiKeySetFlow: Flow<Boolean> = _apiKey.map { !it.isNullOrBlank() }
    override suspend fun saveGeminiApiKey(apiKey: String?) {
        _apiKey.value = apiKey
    }

    override val useByokKey: Flow<Boolean> = _useByokKey.asStateFlow()
    override suspend fun saveUseByokKey(enabled: Boolean) {
        _useByokKey.value = enabled
    }

    override val useLocalAi: Flow<Boolean> = _useLocalAi.asStateFlow()
    override suspend fun saveUseLocalAi(enabled: Boolean) {
        _useLocalAi.value = enabled
    }

    override val useFirebaseVertexAi: Flow<Boolean> = _useFirebase.asStateFlow()
    override suspend fun saveUseFirebaseVertexAi(enabled: Boolean) {
        _useFirebase.value = enabled
    }
}
