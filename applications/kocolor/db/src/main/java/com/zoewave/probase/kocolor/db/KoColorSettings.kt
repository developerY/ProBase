package com.zoewave.probase.kocolor.db

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.zoewave.probase.core.data.repository.SecureApiKeyRepository
import com.zoewave.probase.features.ai.capture.domain.SmartCaptureSettings
import com.zoewave.probase.core.data.repository.AiConfigurationSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "kocolor_settings")

@Singleton
class KoColorSettings @Inject constructor(
    @ApplicationContext private val context: Context,
    private val secureApiKeyRepository: SecureApiKeyRepository
) : AiConfigurationSettings, SmartCaptureSettings {

    private object PreferencesKeys {
        val IS_AI_ENABLED = booleanPreferencesKey("is_ai_enabled")
        val AI_MODEL = stringPreferencesKey("ai_model")
        val APP_THEME = stringPreferencesKey("app_theme")
        val COLOR_PALETTE = stringPreferencesKey("color_palette")
        val HYDRATION_GOAL = androidx.datastore.preferences.core.doublePreferencesKey("hydration_goal")
    }

    val appThemeFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.APP_THEME] ?: "SYSTEM"
    }

    suspend fun saveAppTheme(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.APP_THEME] = theme
        }
    }

    val colorPaletteFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.COLOR_PALETTE] ?: "CLASSIC"
    }

    suspend fun saveColorPalette(palette: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.COLOR_PALETTE] = palette
        }
    }

    val hydrationGoalFlow: Flow<Double> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.HYDRATION_GOAL] ?: 2.7 // Updated default as requested
    }

    suspend fun saveHydrationGoal(goal: Double) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HYDRATION_GOAL] = goal
        }
    }

    // AiConfigurationSettings & SmartCaptureSettings
    override val isAiEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_AI_ENABLED] ?: false
    }

    override suspend fun saveAiEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_AI_ENABLED] = enabled
        }
    }

    override val aiModelFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.AI_MODEL] ?: "gemini-1.5-flash"
    }

    override val userAiModelFlow: Flow<String> = aiModelFlow

    override suspend fun saveAiModel(model: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AI_MODEL] = model
        }
    }

    override fun getGeminiApiKey(): String? {
        return secureApiKeyRepository.getKey()
    }

    override val isGeminiApiKeySetFlow: Flow<Boolean> = secureApiKeyRepository.isKeySetFlow
    override val userApiKeyFlow: Flow<String?> = isGeminiApiKeySetFlow.map { if (it) getGeminiApiKey() else null }

    override suspend fun saveGeminiApiKey(apiKey: String?) {
        if (apiKey == null) {
            secureApiKeyRepository.deleteKey()
        } else {
            secureApiKeyRepository.saveKey(apiKey)
        }
    }
}
