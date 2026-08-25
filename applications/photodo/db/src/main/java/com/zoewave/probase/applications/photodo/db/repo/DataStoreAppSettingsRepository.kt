package com.zoewave.probase.applications.photodo.db.repo

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.zoewave.probase.core.data.repository.SecureApiKeyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreAppSettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val secureApiKeyRepository: SecureApiKeyRepository
) : AppSettingsRepository {

    private val PALETTE_KEY = stringPreferencesKey("palette_preference")

    override val palettePreferenceFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[PALETTE_KEY] ?: "DEFAULT"
    }

    // Add the save function
    override suspend fun savePalettePreference(paletteIdentifier: String) {
        dataStore.edit { preferences ->
            preferences[PALETTE_KEY] = paletteIdentifier
        }
    }

    private val THEME_KEY = stringPreferencesKey("theme_preference")

    override val themePreferenceFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[THEME_KEY] ?: "SYSTEM"
    }

    override suspend fun saveThemePreference(themeIdentifier: String) {
        dataStore.edit { preferences ->
            preferences[THEME_KEY] = themeIdentifier
        }
    }

    private val PANE_CONTRAST_KEY = stringPreferencesKey("pane_contrast_preference")

    override val paneContrastFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[PANE_CONTRAST_KEY] ?: "TINTED"
    }

    override suspend fun savePaneContrast(paneContrastIdentifier: String) {
        dataStore.edit { preferences ->
            preferences[PANE_CONTRAST_KEY] = paneContrastIdentifier
        }
    }

    override fun getGeminiApiKey(): String? {
        return secureApiKeyRepository.getKey()
    }

    override val isGeminiApiKeySetFlow: Flow<Boolean> = secureApiKeyRepository.isKeySetFlow

    override suspend fun saveGeminiApiKey(apiKey: String?) {
        if (apiKey == null) {
            secureApiKeyRepository.deleteKey()
        } else {
            secureApiKeyRepository.saveKey(apiKey)
        }
    }

    private val IS_AI_ENABLED_KEY = booleanPreferencesKey("is_ai_enabled")

    override val isAiEnabledFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[IS_AI_ENABLED_KEY] ?: false
    }

    override suspend fun saveAiEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_AI_ENABLED_KEY] = enabled
        }
    }

    private val AI_MODEL_KEY = stringPreferencesKey("ai_model_preference")

    override val aiModelFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[AI_MODEL_KEY] ?: "gemini-1.5-flash"
    }

    override suspend fun saveAiModel(model: String) {
        dataStore.edit { preferences ->
            preferences[AI_MODEL_KEY] = model
        }
    }

    private val ANIMATIONS_ENABLED_KEY = booleanPreferencesKey("animations_enabled")

    override val animationsEnabledFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[ANIMATIONS_ENABLED_KEY] ?: true
    }

    override suspend fun saveAnimationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[ANIMATIONS_ENABLED_KEY] = enabled
        }
    }

    override val useFirebaseVertexAi: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[booleanPreferencesKey("use_firebase_vertex_ai")] ?: true
    }

    override suspend fun saveUseFirebaseVertexAi(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[booleanPreferencesKey("use_firebase_vertex_ai")] = enabled
        }
    }
}
