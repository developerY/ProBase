package com.zoewave.probase.applications.photodo.db.repo

import kotlinx.coroutines.flow.Flow

interface AppSettingsRepository {
    val themePreferenceFlow: Flow<String>
    suspend fun saveThemePreference(themeIdentifier: String)
    val palettePreferenceFlow: Flow<String>
    suspend fun savePalettePreference(paletteIdentifier: String)
    val paneContrastFlow: Flow<String>
    suspend fun savePaneContrast(paneContrastIdentifier: String)

    val geminiApiKeyFlow: Flow<String?>
    suspend fun saveGeminiApiKey(apiKey: String?)

    val isAiEnabledFlow: Flow<Boolean>
    suspend fun saveAiEnabled(enabled: Boolean)
}
