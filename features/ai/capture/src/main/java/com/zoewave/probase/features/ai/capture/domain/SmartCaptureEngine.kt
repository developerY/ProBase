package com.zoewave.probase.features.ai.capture.domain

import android.graphics.Bitmap

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
        modelName: String? = null,
        userContext: String? = null
    ): DiagnosticResult

    /**
     * Fetches the list of supported models from the engine.
     */
    suspend fun getAvailableModels(apiKey: String?): List<String> = emptyList()

    /**
     * Tests a specific model with a simple prompt.
     */
    suspend fun testModel(apiKey: String, modelName: String): String = "Not Supported"
}
