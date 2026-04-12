package com.zoewave.probase.features.smartcapture.domain

import kotlinx.coroutines.flow.Flow

/**
 * Isolated settings interface for the Smart Capture module.
 * Decoupled from the main app's database.
 */
interface SmartCaptureSettings {
    val userApiKeyFlow: Flow<String?>
}
