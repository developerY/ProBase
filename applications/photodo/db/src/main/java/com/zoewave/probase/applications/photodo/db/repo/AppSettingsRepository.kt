package com.zoewave.probase.applications.photodo.db.repo

import kotlinx.coroutines.flow.Flow

interface AppSettingsRepository {
    val themePreferenceFlow: Flow<String>
    suspend fun saveThemePreference(themeIdentifier: String)
}