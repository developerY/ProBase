package com.zoewave.probase.gotmind.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.zoewave.probase.gotmind.model.AppTheme
import com.zoewave.probase.gotmind.model.ColorPalette
import com.zoewave.probase.gotmind.model.ThemeSettings
import com.zoewave.probase.gotmind.model.GameSettings
import com.zoewave.probase.gotmind.model.MemBloxEngineType
import com.zoewave.probase.gotmind.model.MindWaveMode
import com.zoewave.probase.gotmind.model.NodeShape
import com.zoewave.probase.gotmind.model.InstrumentType
import com.zoewave.probase.gotmind.data.di.GotMindDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface AppSettingsRepository {
    val themeSettingsFlow: Flow<ThemeSettings>
    val gameSettingsFlow: Flow<GameSettings>
    
    suspend fun saveTheme(theme: AppTheme)
    suspend fun savePalette(palette: ColorPalette)
    
    suspend fun saveMemBloxEngineType(type: MemBloxEngineType)
    suspend fun saveMemBloxSpeed(speed: Float)
    suspend fun saveMemBloxDropHeight(height: Int)
    suspend fun saveMemBloxDropDuration(durationMillis: Int)
    suspend fun saveHapticsEnabled(enabled: Boolean)
    suspend fun saveSoundEnabled(enabled: Boolean)
    suspend fun saveMindWaveMode(mode: MindWaveMode)
    suspend fun saveMindWaveNodeShape(shape: NodeShape)
    suspend fun saveInstrumentType(type: InstrumentType)
    suspend fun saveSongMasterEnabled(enabled: Boolean)
}

@Singleton
class AppSettingsRepositoryImpl @Inject constructor(
    @GotMindDataStore private val dataStore: DataStore<Preferences>
) : AppSettingsRepository {

    private val THEME_KEY = stringPreferencesKey("app_theme")
    private val PALETTE_KEY = stringPreferencesKey("color_palette")

    private val MB_ENGINE_KEY = stringPreferencesKey("mb_engine_type")
    private val MB_SPEED_KEY = floatPreferencesKey("mb_game_speed")
    private val MB_HEIGHT_KEY = intPreferencesKey("mb_drop_height")
    private val MB_DURATION_KEY = intPreferencesKey("mb_drop_duration")
    private val HAPTICS_KEY = booleanPreferencesKey("haptics_enabled")
    private val SOUND_KEY = booleanPreferencesKey("sound_enabled")
    private val MW_MODE_KEY = stringPreferencesKey("mw_mode")
    private val MW_SHAPE_KEY = stringPreferencesKey("mw_node_shape")
    private val MW_INSTRUMENT_KEY = stringPreferencesKey("mw_instrument")
    private val MW_SONG_MASTER_KEY = booleanPreferencesKey("mw_song_master")

    override val themeSettingsFlow: Flow<ThemeSettings> = dataStore.data.map { preferences ->
        ThemeSettings(
            theme = AppTheme.valueOf(preferences[THEME_KEY] ?: AppTheme.SYSTEM.name),
            palette = ColorPalette.valueOf(preferences[PALETTE_KEY] ?: ColorPalette.DEFAULT.name)
        )
    }

    override val gameSettingsFlow: Flow<GameSettings> = dataStore.data.map { preferences ->
        GameSettings(
            engineType = MemBloxEngineType.valueOf(preferences[MB_ENGINE_KEY] ?: MemBloxEngineType.STATIC.name),
            gameSpeed = preferences[MB_SPEED_KEY] ?: 1.0f,
            dropHeight = preferences[MB_HEIGHT_KEY] ?: 5,
            dropDurationMillis = preferences[MB_DURATION_KEY] ?: 3000,
            hapticsEnabled = preferences[HAPTICS_KEY] ?: true,
            soundEnabled = preferences[SOUND_KEY] ?: true,
            mindWaveMode = MindWaveMode.valueOf(preferences[MW_MODE_KEY] ?: MindWaveMode.CLASSIC.name),
            mindWaveNodeShape = NodeShape.valueOf(preferences[MW_SHAPE_KEY] ?: NodeShape.CIRCLE.name),
            instrumentType = InstrumentType.valueOf(preferences[MW_INSTRUMENT_KEY] ?: InstrumentType.CLEAN_SYNTH.name),
            songMasterEnabled = preferences[MW_SONG_MASTER_KEY] ?: false
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

    override suspend fun saveMemBloxEngineType(type: MemBloxEngineType) {
        dataStore.edit { preferences ->
            preferences[MB_ENGINE_KEY] = type.name
        }
    }

    override suspend fun saveMemBloxSpeed(speed: Float) {
        dataStore.edit { preferences ->
            preferences[MB_SPEED_KEY] = speed
        }
    }

    override suspend fun saveMemBloxDropHeight(height: Int) {
        dataStore.edit { preferences ->
            preferences[MB_HEIGHT_KEY] = height
        }
    }

    override suspend fun saveMemBloxDropDuration(durationMillis: Int) {
        dataStore.edit { preferences ->
            preferences[MB_DURATION_KEY] = durationMillis
        }
    }

    override suspend fun saveHapticsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[HAPTICS_KEY] = enabled
        }
    }

    override suspend fun saveSoundEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[SOUND_KEY] = enabled
        }
    }

    override suspend fun saveMindWaveMode(mode: MindWaveMode) {
        dataStore.edit { preferences ->
            preferences[MW_MODE_KEY] = mode.name
        }
    }

    override suspend fun saveMindWaveNodeShape(shape: NodeShape) {
        dataStore.edit { preferences ->
            preferences[MW_SHAPE_KEY] = shape.name
        }
    }

    override suspend fun saveInstrumentType(type: InstrumentType) {
        dataStore.edit { preferences ->
            preferences[MW_INSTRUMENT_KEY] = type.name
        }
    }

    override suspend fun saveSongMasterEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[MW_SONG_MASTER_KEY] = enabled
        }
    }
}
