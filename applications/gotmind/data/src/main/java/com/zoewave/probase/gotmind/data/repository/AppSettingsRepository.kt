package com.zoewave.probase.gotmind.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.zoewave.probase.gotmind.model.AppTheme
import com.zoewave.probase.gotmind.model.ColorPalette
import com.zoewave.probase.gotmind.model.ThemeSettings
import com.zoewave.probase.gotmind.data.di.GotMindDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface AppSettingsRepository {
    val themeSettingsFlow: Flow<ThemeSettings>
    suspend fun saveTheme(theme: AppTheme)
    suspend fun savePalette(palette: ColorPalette)
}

@Singleton
class AppSettingsRepositoryImpl @Inject constructor(
    @GotMindDataStore private val dataStore: DataStore<Preferences>
) : AppSettingsRepository {

    private val THEME_KEY = stringPreferencesKey("app_theme")
    private val PALETTE_KEY = stringPreferencesKey("color_palette")

    override val themeSettingsFlow: Flow<ThemeSettings> = dataStore.data.map { preferences ->
        ThemeSettings(
            theme = AppTheme.valueOf(preferences[THEME_KEY] ?: AppTheme.SYSTEM.name),
            palette = ColorPalette.valueOf(preferences[PALETTE_KEY] ?: ColorPalette.DEFAULT.name)
        )
    }

    override suspend fun saveTheme(theme: AppTheme) {
        dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme.name
        }
    }

    override suspend fun savePalette(palette: ColorPalette) {
        dataStore.edit { preferences ->
            preferences[PALETTE_KEY] = palette.name
        }
    }
}
