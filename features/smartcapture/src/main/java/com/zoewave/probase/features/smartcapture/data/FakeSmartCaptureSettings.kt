package com.zoewave.probase.features.smartcapture.data

import com.zoewave.probase.features.smartcapture.domain.SmartCaptureSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

/**
 * A fake implementation of settings for development/isolation testing.
 * In a real app, this would be implemented in the app module or a settings module.
 */
class FakeSmartCaptureSettings @Inject constructor() : SmartCaptureSettings {
    override val userApiKeyFlow: Flow<String?> = flowOf(null) // BYOK: Default to null to trigger local fallback
    override val userAiModelFlow: Flow<String> = flowOf("gemini-1.5-flash")
}
