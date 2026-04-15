package com.zoewave.probase.seaweed.mobile.di

import com.zoewave.probase.features.ai.capture.domain.SmartCaptureSettings
import com.zoewave.probase.seaweed.data.UserSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RealSmartCaptureSettings @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository
) : SmartCaptureSettings {
    override val userApiKeyFlow: Flow<String?> = userSettingsRepository.getUserSettings().map { 
        userSettingsRepository.getGeminiApiKey() 
    }
    override val userAiModelFlow: Flow<String> = userSettingsRepository.aiModelFlow
}
