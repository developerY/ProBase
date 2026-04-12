package com.zoewave.probase.features.smartcapture.domain

import android.graphics.Bitmap

/**
 * Common interface for multimodal task parsing engines.
 */
interface SmartCaptureEngine {
    /**
     * Processes an image and attempts to parse it into a structured task.
     * @param bitmap The image to analyze.
     * @param apiKey Optional API key for cloud engines.
     */
    suspend fun processImage(bitmap: Bitmap, apiKey: String?): TaskDraftState
}
