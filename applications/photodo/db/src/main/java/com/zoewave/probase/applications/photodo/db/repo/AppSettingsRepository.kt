package com.zoewave.probase.applications.photodo.db.repo

import com.zoewave.probase.features.ai.configuration.domain.AiConfigurationSettings
import kotlinx.coroutines.flow.Flow

interface AppSettingsRepository : AiConfigurationSettings {
    val themePreferenceFlow: Flow<String>
    suspend fun saveThemePreference(themeIdentifier: String)
    val palettePreferenceFlow: Flow<String>
    suspend fun savePalettePreference(paletteIdentifier: String)
    val paneContrastFlow: Flow<String>
    suspend fun savePaneContrast(paneContrastIdentifier: String)

    // Secure Storage for API Key
    fun getGeminiApiKey(): String?
    val isGeminiApiKeySetFlow: Flow<Boolean>
    suspend fun saveGeminiApiKey(apiKey: String?)

    val isAiEnabledFlow: Flow<Boolean>
    suspend fun saveAiEnabled(enabled: Boolean)

    val aiModelFlow: Flow<String>
    suspend fun saveAiModel(model: String)
}
