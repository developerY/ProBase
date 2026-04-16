package com.zoewave.probase.applications.photodo.db.di

import com.zoewave.probase.applications.photodo.db.repo.AppSettingsRepository
import com.zoewave.probase.features.ai.capture.domain.SmartCaptureSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RealSmartCaptureSettings @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository
) : SmartCaptureSettings {
    override val userApiKeyFlow: Flow<String?> = flow {
        emit(appSettingsRepository.getGeminiApiKey())
    }
    override val userAiModelFlow: Flow<String> = appSettingsRepository.aiModelFlow
}
