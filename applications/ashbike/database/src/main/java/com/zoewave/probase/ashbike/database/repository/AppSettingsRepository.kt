package com.zoewave.probase.ashbike.database.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.zoewave.ashbike.model.bike.LocationEnergyLevel
import com.zoewave.probase.core.data.repository.SecureApiKeyRepository
import com.zoewave.probase.features.ai.capture.domain.SmartCaptureSettings
import com.zoewave.probase.core.data.repository.AiConfigurationSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// 2a) Define the keys you need
private object SettingsPrefsKeys {
    val THEME = stringPreferencesKey("settings_theme")
    val LANGUAGE = stringPreferencesKey("settings_language")
    val NOTIFICATIONS = stringPreferencesKey("settings_notifications")
    val UNITS = stringPreferencesKey("settings_units") // Added Units Key
    val GPS_ACCURACY = stringPreferencesKey("settings_gps_accuracy")
    val LONG_RIDE_ENABLED =
        booleanPreferencesKey("settings_long_ride_enabled") // Added Long Ride Key
    val IS_AI_ENABLED = booleanPreferencesKey("is_ai_enabled")
    val AI_MODEL = stringPreferencesKey("ai_model")
}

// 2b) Repository contract – your ViewModel only talks to this
interface AppSettingsRepository : AiConfigurationSettings, SmartCaptureSettings {
    val themeFlow: Flow<String>
    val languageFlow: Flow<String>
    val notificationsFlow: Flow<String>
    val unitsFlow: Flow<String> // Added Units Flow
    val gpsAccuracyFlow: Flow<LocationEnergyLevel>
    val longRideEnabledFlow: Flow<Boolean> // Added Long Ride Flow

    suspend fun setTheme(theme: String)
    suspend fun setLanguage(language: String)
    suspend fun setNotifications(option: String)
    suspend fun setUnits(units: String) // Added setUnits function
    suspend fun setGpsAccuracy(accuracy: String)
    suspend fun setLongRideEnabled(enabled: Boolean) // Added setLongRideEnabled function
}

// 2c) DataStore-backed impl
@Singleton
class DataStoreAppSettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val secureApiKeyRepository: SecureApiKeyRepository
) : AppSettingsRepository {

    override val themeFlow: Flow<String> = dataStore.data
        .map { it[SettingsPrefsKeys.THEME] ?: "System" }

    override val languageFlow: Flow<String> = dataStore.data
        .map { it[SettingsPrefsKeys.LANGUAGE] ?: "English" }

    override val notificationsFlow: Flow<String> = dataStore.data
        .map { it[SettingsPrefsKeys.NOTIFICATIONS] ?: "Enabled" }

    override val unitsFlow: Flow<String> = dataStore.data // Added Units Flow implementation
        .map { it[SettingsPrefsKeys.UNITS] ?: "Metric (SI)" } // Defaulting to Metric

    override val gpsAccuracyFlow: Flow<LocationEnergyLevel> = dataStore.data
        .map { preferences ->
            val accuracyString =
                preferences[SettingsPrefsKeys.GPS_ACCURACY] ?: LocationEnergyLevel.BALANCED.name
            try {
                LocationEnergyLevel.valueOf(accuracyString)
            } catch (e: IllegalArgumentException) {
                // If the stored string is not a valid enum member, default to BALANCED.
                // This handles potential data corruption or older invalid values.
                LocationEnergyLevel.BALANCED
            }
        }

    override val longRideEnabledFlow: Flow<Boolean> =
        dataStore.data // Added Long Ride Flow implementation
            .map { it[SettingsPrefsKeys.LONG_RIDE_ENABLED] ?: false } // Defaulting to false

    // AI Configuration Implementation
    override val isAiEnabledFlow: Flow<Boolean> = dataStore.data.map {
        it[SettingsPrefsKeys.IS_AI_ENABLED] ?: false
    }

    override suspend fun saveAiEnabled(enabled: Boolean) {
        dataStore.edit { it[SettingsPrefsKeys.IS_AI_ENABLED] = enabled }
    }

    override val aiModelFlow: Flow<String> = dataStore.data.map {
        it[SettingsPrefsKeys.AI_MODEL] ?: "gemini-1.5-flash"
    }

    override suspend fun saveAiModel(model: String) {
        dataStore.edit { it[SettingsPrefsKeys.AI_MODEL] = model }
    }

    override val useFirebaseVertexAi: Flow<Boolean> = dataStore.data.map {
        it[booleanPreferencesKey("use_firebase_vertex_ai")] ?: true
    }

    override suspend fun saveUseFirebaseVertexAi(enabled: Boolean) {
        dataStore.edit { it[booleanPreferencesKey("use_firebase_vertex_ai")] = enabled }
    }

    override fun getGeminiApiKey(): String? = secureApiKeyRepository.getKey()

    override val isGeminiApiKeySetFlow: Flow<Boolean> = secureApiKeyRepository.isKeySetFlow

    override suspend fun saveGeminiApiKey(apiKey: String?) {
        if (apiKey == null) {
            secureApiKeyRepository.deleteKey()
        } else {
            secureApiKeyRepository.saveKey(apiKey)
        }
    }

    // SmartCaptureSettings Implementation
    override val userApiKeyFlow: Flow<String?> = isGeminiApiKeySetFlow.map { 
        if (it) getGeminiApiKey() else null 
    }
    
    override val userAiModelFlow: Flow<String> = aiModelFlow

    override suspend fun setTheme(theme: String) {
        dataStore.edit { prefs -> prefs[SettingsPrefsKeys.THEME] = theme }
    }

    override suspend fun setLanguage(language: String) {
        dataStore.edit { prefs -> prefs[SettingsPrefsKeys.LANGUAGE] = language }
    }

    override suspend fun setNotifications(option: String) {
        dataStore.edit { prefs -> prefs[SettingsPrefsKeys.NOTIFICATIONS] = option }
    }

    override suspend fun setUnits(units: String) { // Added setUnits implementation
        dataStore.edit { prefs -> prefs[SettingsPrefsKeys.UNITS] = units }
    }

    override suspend fun setGpsAccuracy(accuracy: String) {
        dataStore.edit { prefs -> prefs[SettingsPrefsKeys.GPS_ACCURACY] = accuracy }
    }

    override suspend fun setLongRideEnabled(enabled: Boolean) { // Added setLongRideEnabled implementation
        dataStore.edit { prefs -> prefs[SettingsPrefsKeys.LONG_RIDE_ENABLED] = enabled }
    }
}
