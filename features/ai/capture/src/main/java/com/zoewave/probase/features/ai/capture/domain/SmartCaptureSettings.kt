package com.zoewave.probase.features.ai.capture.domain

import kotlinx.coroutines.flow.Flow

/**
 * Isolated settings interface for the Smart Capture module.
 * Decoupled from the main app's database.
 */
interface SmartCaptureSettings {
    val userApiKeyFlow: Flow<String?>
    val userAiModelFlow: Flow<String>
}
