package com.zoewave.probase.photodo.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.zoewave.probase.photodo.model.sync.SyncCategory
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "photodo_sync_prefs")

@Singleton
class SyncDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = "SyncDataStore"
    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        private val LATEST_SYNC_PAYLOAD = stringPreferencesKey("latest_sync_payload")
    }

    val latestSyncDataFlow: Flow<List<SyncCategory>> = context.dataStore.data
        .map { preferences ->
            val payload = preferences[LATEST_SYNC_PAYLOAD] ?: return@map emptyList<SyncCategory>()
            try {
                json.decodeFromString<List<SyncCategory>>(payload)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to decode sync payload", e)
                emptyList()
            }
        }

    suspend fun saveLatestSyncPayload(payload: String) {
        context.dataStore.edit { preferences ->
            preferences[LATEST_SYNC_PAYLOAD] = payload
        }
    }
}
