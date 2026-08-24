package com.zoewave.probase.core.data.repository

import kotlinx.coroutines.flow.Flow

/**
 * Common settings interface for AI configuration.
 * Apps must implement this to provide persistence for AI preferences.
 */
interface AiConfigurationSettings {
    val isAiEnabledFlow: Flow<Boolean>
    suspend fun saveAiEnabled(enabled: Boolean)

    val aiModelFlow: Flow<String>
    suspend fun saveAiModel(model: String)

    // Secure Storage delegation
    fun getGeminiApiKey(): String?
    val isGeminiApiKeySetFlow: Flow<Boolean>
    suspend fun saveGeminiApiKey(apiKey: String?)

    // Firebase Vertex AI
    val useFirebaseVertexAi: Flow<Boolean>
    suspend fun saveUseFirebaseVertexAi(enabled: Boolean)
}
