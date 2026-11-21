package com.ylabz.basepro.applications.bike.database

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.zoewave.ashbike.model.bike.LocationEnergyLevel
import com.zoewave.probase.ashbike.database.repository.DataStoreAppSettingsRepository
import com.zoewave.probase.core.data.repository.SecureApiKeyRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class DataStoreAppSettingsRepositoryTest {

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var secureApiKeyRepository: SecureApiKeyRepository
    private lateinit var repository: DataStoreAppSettingsRepository

    private val preferencesFlow = MutableStateFlow<Preferences>(emptyPreferences())

    @Before
    fun setUp() {
        dataStore = mockk()
        every { dataStore.data } returns preferencesFlow

        // Mock edit / updateData for DataStore
        coEvery { dataStore.updateData(any()) } coAnswers {
            val transform = arg<suspend (Preferences) -> Preferences>(0) as suspend (Preferences) -> Preferences
            val updated = transform(preferencesFlow.value)
            preferencesFlow.value = updated
            updated
        }

        secureApiKeyRepository = mockk()
        every { secureApiKeyRepository.isKeySetFlow } returns kotlinx.coroutines.flow.MutableStateFlow(false) as kotlinx.coroutines.flow.StateFlow<Boolean>

        repository = DataStoreAppSettingsRepository(dataStore, secureApiKeyRepository)
    }

    @Test
    fun `themeFlow returns default when not set`() = runTest {
        val theme = repository.themeFlow.first()
        assertEquals("System", theme)
    }

    @Test
    fun `languageFlow returns set value`() = runTest {
        val prefs = mutablePreferencesOf(stringPreferencesKey("settings_language") to "French")
        preferencesFlow.value = prefs

        val language = repository.languageFlow.first()
        assertEquals("French", language)
    }

    @Test
    fun `gpsAccuracyFlow returns BALANCED on invalid enum`() = runTest {
        val prefs = mutablePreferencesOf(stringPreferencesKey("settings_gps_accuracy") to "INVALID_LEVEL")
        preferencesFlow.value = prefs

        val accuracy = repository.gpsAccuracyFlow.first()
        assertEquals(LocationEnergyLevel.BALANCED, accuracy)
    }

    @Test
    fun `isGeminiApiKeySetFlow maps from secureApiKeyRepository`() = runTest {
        val flow = MutableStateFlow(true)
        every { secureApiKeyRepository.isKeySetFlow } returns flow as kotlinx.coroutines.flow.StateFlow<Boolean>

        val isSet = repository.isGeminiApiKeySetFlow.first()
        assertEquals(true, isSet)
    }
}
