package com.ylabz.basepro.applications.bike.database

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.zoewave.probase.ashbike.database.ProfileData
import com.zoewave.probase.ashbike.database.repository.DataStoreUserProfileRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DataStoreUserProfileRepositoryTest {

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var repository: DataStoreUserProfileRepository

    private val preferencesFlow = MutableStateFlow<Preferences>(emptyPreferences())

    @Before
    fun setUp() {
        dataStore = mockk()
        every { dataStore.data } returns preferencesFlow

        coEvery { dataStore.updateData(any()) } answers {
            val transform = arg<(Preferences) -> Preferences>(0)
            val updated = transform(preferencesFlow.value)
            preferencesFlow.value = updated
            updated
        }

        repository = DataStoreUserProfileRepository(dataStore)
    }

    @Test
    fun `nameFlow returns default when empty`() = runTest {
        val name = repository.nameFlow.first()
        assertEquals("Ash Monster", name)
    }

    @Test
    fun `nameFlow returns set value`() = runTest {
        val prefs = mutablePreferencesOf(stringPreferencesKey("user_name") to "Zoe")
        preferencesFlow.value = prefs
        val name = repository.nameFlow.first()
        assertEquals("Zoe", name)
    }

    @Test
    fun `profileReviewedOrSavedFlow returns default false`() = runTest {
        val reviewed = repository.profileReviewedOrSavedFlow.first()
        assertEquals(false, reviewed)
    }

    @Test
    fun `saveProfile updates all fields and reviewed flag`() = runTest {
        val profile = ProfileData(name = "Zoe", heightCm = "165", weightKg = "60")
        repository.saveProfile(profile)

        val updatedPrefs = preferencesFlow.value
        assertEquals("Zoe", updatedPrefs[stringPreferencesKey("user_name")])
        assertEquals("165", updatedPrefs[stringPreferencesKey("user_height_cm")])
        assertEquals("60", updatedPrefs[stringPreferencesKey("user_weight_kg")])
        assertTrue(updatedPrefs[booleanPreferencesKey("profile_reviewed_or_saved_by_user")] == true)
    }
}
