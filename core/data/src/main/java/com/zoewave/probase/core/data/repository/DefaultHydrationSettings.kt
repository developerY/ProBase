package com.zoewave.probase.core.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "hydration_settings")

@Singleton
class DefaultHydrationSettings @Inject constructor(
    @ApplicationContext private val context: Context
) : HydrationSettings {

    private object PreferencesKeys {
        val HYDRATION_GOAL = doublePreferencesKey("hydration_goal")
    }

    override val hydrationGoalFlow: Flow<Double> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.HYDRATION_GOAL] ?: 2.7
    }

    override suspend fun saveHydrationGoal(goal: Double) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HYDRATION_GOAL] = goal
        }
    }
}
