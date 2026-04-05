package com.zoewave.probase.seaweed.data

import com.zoewave.probase.seaweed.model.UserSettings
import kotlinx.coroutines.flow.Flow

interface UserSettingsRepository {
    fun getUserSettings(): Flow<UserSettings>
    suspend fun saveUserSettings(settings: UserSettings)
}
