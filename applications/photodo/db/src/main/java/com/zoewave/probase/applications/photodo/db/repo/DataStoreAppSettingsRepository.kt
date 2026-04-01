package com.zoewave.probase.applications.photodo.db.repo

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreAppSettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
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
}
