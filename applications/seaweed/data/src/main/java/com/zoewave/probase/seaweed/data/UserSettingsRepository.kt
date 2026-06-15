package com.zoewave.probase.seaweed.data

import com.zoewave.probase.features.ai.capture.domain.SmartCaptureSettings
import com.zoewave.probase.core.data.repository.AiConfigurationSettings
import com.zoewave.probase.seaweed.model.UserSettings
import kotlinx.coroutines.flow.Flow

interface UserSettingsRepository : AiConfigurationSettings, SmartCaptureSettings {
    fun getUserSettings(): Flow<UserSettings>
    suspend fun saveUserSettings(settings: UserSettings)
}
