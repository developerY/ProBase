package com.zoewave.probase.features.smartcapture.domain

import android.graphics.Bitmap
import com.zoewave.probase.core.model.tasks.SmartTaskDraft

/**
 * Common interface for multimodal task parsing engines.
 */
interface SmartCaptureEngine {
    /**
     * Processes an image and attempts to parse it into a structured task.
     * @param bitmap The image to analyze.
     * @param apiKey Optional API key for cloud engines.
     * @param modelName Optional model name for cloud engines.
     */
    suspend fun processImage(
        bitmap: Bitmap,
        apiKey: String?,
        modelName: String? = null
    ): DiagnosticResult
}
