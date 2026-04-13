package com.zoewave.probase.di

import com.zoewave.probase.features.smartcapture.domain.SmartCaptureSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeSmartCaptureSettings @Inject constructor() : SmartCaptureSettings {
    override val userApiKeyFlow: Flow<String?> = flowOf(null)
    override val userAiModelFlow: Flow<String> = flowOf("gemini-1.5-flash")
}
