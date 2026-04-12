package com.zoewave.probase.photodo.mobile.di

import com.zoewave.probase.applications.photodo.db.repo.AppSettingsRepository
import com.zoewave.probase.features.smartcapture.domain.SmartCaptureSettings
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RealSmartCaptureSettings @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository
) : SmartCaptureSettings {
    override val userApiKeyFlow: Flow<String?> = appSettingsRepository.geminiApiKeyFlow
}
