package com.zoewave.probase.features.ai.local.data

import android.util.Log
import com.google.mlkit.genai.common.DownloadStatus
import com.google.mlkit.genai.prompt.Generation
import com.google.mlkit.genai.prompt.GenerationConfig
import com.google.mlkit.genai.prompt.GenerativeModel
import com.google.mlkit.genai.prompt.ModelConfig
import com.google.mlkit.genai.prompt.ModelPreference
import com.google.mlkit.genai.prompt.ModelReleaseStage
import com.google.mlkit.genai.prompt.generationConfig
import com.zoewave.probase.features.ai.local.domain.router.GeminiPipelineRouter
import com.zoewave.probase.features.ai.local.domain.router.RequiresCloudException
import com.zoewave.probase.features.readers.ocr.domain.model.BoxPanel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class LocalStandardizedData(
    @SerialName("brand_name") val brand: String? = null,
    @SerialName("product_name") val productName: String? = null,
    @SerialName("volume") val volume: String? = null,
    @SerialName("active_ingredients") val ingredients: List<String> = emptyList(),
    @SerialName("inactive_ingredients") val inactiveIngredients: List<String> = emptyList(),
    // Optional fields preserved for pipeline integrity
    val category: String? = null,
    val size: String? = null,
    val claims: List<String> = emptyList(),
    val directions: String? = null
)

sealed interface NanoState {
    data object Available : NanoState
    data object MultimodalAvailable : NanoState
    data class Downloading(val progress: Int = 0) : NanoState
    data object Unsupported : NanoState
}

/**
 * Technical implementation of the On-Device AI Engine.
 * PIVOTED to use the production-ready ML Kit GenAI Prompt API for Pixel 9a compatibility.
 * This resolves the "Required LLM feature not found" error in AICore Edge SDK.
 */
@Singleton
class LocalAiEngine @Inject constructor() {

    // Use the production-ready ML Kit client with FAST preference for A-series compatibility
    private val localModel: GenerativeModel = Generation.getClient(
        generationConfig {
            modelConfig = ModelConfig.builder().apply {
                releaseStage = ModelReleaseStage.PREVIEW
                preference = ModelPreference.FAST
            }.build()
        }
    )

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Checks if the device hardware supports Gemini Nano and if the model is ready.
     * Uses the ML Kit FeatureStatus API.
     */
    suspend fun checkCapability(): NanoState = withContext(Dispatchers.Default) {
        try {
            val status = localModel.checkStatus()
            Log.d("LocalAiEngine", "ML Kit GenAI Status: $status")
            
            when (status) {
                3 -> { // MODEL_AVAILABLE
                    // Heuristic for multimodal check on 2026 flagships
                    val isFlagship = android.os.Build.MODEL.contains("Pro", ignoreCase = true) || 
                                     android.os.Build.MODEL.contains("Fold", ignoreCase = true)
                    if (isFlagship) NanoState.MultimodalAvailable else NanoState.Available
                }
                2 -> NanoState.Downloading() // MODEL_DOWNLOADING
                1 -> { // MODEL_DOWNLOADABLE
                    Log.i("LocalAiEngine", "Model downloadable. Triggering background download...")
                    triggerBackgroundDownload()
                    NanoState.Downloading()
                }
                else -> NanoState.Unsupported
            }
        } catch (e: Exception) {
            val msg = e.message ?: ""
            if (msg.contains("606") || msg.contains("FEATURE_NOT_FOUND")) {
                Log.w("LocalAiEngine", "Feature 636 missing. Attempting explicit download...")
                triggerBackgroundDownload()
                return@withContext NanoState.Downloading()
            }
            Log.e("LocalAiEngine", "ML Kit Status Check Failed: $msg")
            NanoState.Unsupported
        }
    }

    private suspend fun triggerBackgroundDownload() {
        // Collect the flow to track progress in logs
        withContext(Dispatchers.IO) {
            try {
                localModel.download().collect { status ->
                    when (status) {
                        is DownloadStatus.DownloadStarted -> {
                            Log.d("LocalAiEngine", "Download Started: Total size ${status.bytesToDownload / 1024} KB")
                        }
                        is DownloadStatus.DownloadProgress -> {
                            Log.v("LocalAiEngine", "Download Progress: ${status.totalBytesDownloaded / 1024} KB")
                        }
                        is DownloadStatus.DownloadCompleted -> {
                            Log.i("LocalAiEngine", "Download Completed! Gemini Nano is ready.")
                        }
                        is DownloadStatus.DownloadFailed -> {
                            Log.e("LocalAiEngine", "Download Failed", status.e)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("LocalAiEngine", "Error collecting download flow", e)
            }
        }
    }

    /**
     * Generic structured content generation via Gemini Nano.
     * Uses deterministic timeouts and handles requires-cloud handoffs.
     */
    suspend fun generateStructuredContent(
        prompt: String,
        jsonSchema: String? = null
    ): Result<String> = withContext(Dispatchers.Default) {
        try {
            val capability = checkCapability()
            if (capability == NanoState.Unsupported) {
                return@withContext Result.failure(RequiresCloudException("Hardware not supported"))
            }

            val finalPrompt = if (jsonSchema != null) {
                "$prompt\n\nReturn ONLY a valid JSON object matching this schema:\n$jsonSchema"
            } else prompt

            val response = withTimeout(10000) {
                localModel.generateContent(finalPrompt)
            }
            
            val text = response.candidates.firstOrNull()?.text 
                ?: return@withContext Result.failure(Exception("Empty AI response"))

            Result.success(text)
        } catch (e: Exception) {
            val reason = e.message ?: "Local AI generation failed"
            Result.failure(RequiresCloudException(reason))
        }
    }

    /**
     * Standardizes categorized OCR text using the GeminiPipelineRouter via ML Kit.
     */
    suspend fun standardizeCategorizedText(
        categorizedText: Map<BoxPanel, String>,
        bitmap: android.graphics.Bitmap? = null
    ): Result<LocalStandardizedData> = withContext(Dispatchers.Default) {
        if (categorizedText.isEmpty() && bitmap == null) return@withContext Result.success(LocalStandardizedData())

        val startTime = System.currentTimeMillis()
        try {
            val capability = checkCapability()
            val isMultimodal = capability == NanoState.MultimodalAvailable
            val isAvailable = capability == NanoState.Available || isMultimodal

            if (!isAvailable) {
                return@withContext Result.failure(RequiresCloudException("Local model not available (State: $capability)"))
            }

            val aggregatedOcr = categorizedText.values.joinToString("\n")

            // 1. Route the request (Hardware Aware + Script Aware)
            val config = GeminiPipelineRouter.route(
                ocrText = aggregatedOcr,
                bitmap = bitmap,
                isMultimodalSupported = isMultimodal,
                isAicoreAvailable = true
            )

            Log.d("LocalAiEngine", "Routing via ML Kit: ${config.path}")

            // 2. Construct final prompt with JSON schema enforcement
            val finalPrompt = "${config.prompt}\n\nSCHEMA:\n${config.jsonSchema}\n\nCONTENT:\n${config.inputOcrText ?: "[IMAGE DATA PROVIDED]"}"

            // 3. Execution (Delegated to ML Kit Prompt API) with DETERMINISTIC TIMEOUT (8s)
            val response = withTimeout(8000) {
                localModel.generateContent(finalPrompt)
            }
            
            val jsonText = response.candidates.firstOrNull()?.text ?: return@withContext Result.failure(Exception("Empty AI response"))
            Log.d("LocalAiEngine", "Raw AI Response: $jsonText")

            // 4. Safe JSON Extraction
            val jsonRegex = Regex("""\{[\s\S]*\}""")
            val match = jsonRegex.find(jsonText) ?: return@withContext Result.failure(Exception("No JSON found in response"))
            
            val result = json.decodeFromString<LocalStandardizedData>(match.value)
            val duration = System.currentTimeMillis() - startTime
            Log.d("LocalAiEngine", "Standardization complete in ${duration}ms. Path: ${config.path}")
            Log.d("LocalAiEngine", "PARSED AI DATA: $result")

            Result.success(result)

        } catch (e: Exception) {
            val reason = e.message ?: "Unknown local AI error"
            Log.w("LocalAiEngine", "Local AI failed: $reason. Triggering Cloud Handoff.")
            Result.failure(RequiresCloudException(reason))
        }
    }

    /**
     * Standardizes raw OCR text using Gemini Nano on-device.
     */
    suspend fun standardizeOcrText(rawText: String): Result<LocalStandardizedData> = withContext(Dispatchers.Default) {
        standardizeCategorizedText(mapOf(BoxPanel.FRONT to rawText))
    }
}
