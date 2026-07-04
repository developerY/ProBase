package com.zoewave.probase.features.ai.local.data

import android.content.Context
import android.util.Log
import com.google.ai.edge.aicore.GenerativeModel
import com.google.ai.edge.aicore.generationConfig
import com.zoewave.probase.features.readers.ocr.domain.model.BoxPanel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class LocalStandardizedData(
    val brand: String? = null,
    val productName: String? = null,
    val category: String? = null,
    val size: String? = null,
    val ingredients: List<String> = emptyList(),
    val claims: List<String> = emptyList(),
    val directions: String? = null
)

sealed interface NanoState {
    data object Available : NanoState
    data object Downloading : NanoState
    data object Unsupported : NanoState
}

/**
 * Technical implementation of the On-Device AI Engine.
 * Corrected to use the Google AI Edge SDK interfacing with Android AICore.
 */
@Singleton
class LocalAiEngine @Inject constructor(
    @ApplicationContext private val context: Context
) {

    // System-Level Abstraction: No API Key, delegated to Android OS
    private val localModel = GenerativeModel(
        generationConfig = generationConfig {
            context = this@LocalAiEngine.context
        }
    )

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Checks if the device hardware supports Gemini Nano and if the model is ready.
     */
    suspend fun checkCapability(): NanoState = withContext(Dispatchers.Default) {
        try {
            localModel.prepareInferenceEngine()
            NanoState.Available
        } catch (e: Exception) {
            val msg = e.message?.lowercase() ?: ""
            when {
                msg.contains("download") -> NanoState.Downloading
                else -> NanoState.Unsupported
            }
        }
    }

    /**
     * Standardizes categorized OCR text using Gemini Nano on-device.
     * Implements silent hardware bypass via Result.failure.
     */
    suspend fun standardizeCategorizedText(categorizedText: Map<BoxPanel, String>): Result<LocalStandardizedData> = withContext(Dispatchers.Default) {
        if (categorizedText.isEmpty()) return@withContext Result.success(LocalStandardizedData())

        val startTime = System.currentTimeMillis()
        try {
            val capability = checkCapability()
            if (capability != NanoState.Available) {
                return@withContext Result.failure(UnsupportedOperationException("Gemini Nano not available: $capability"))
            }

            // Build a perfectly segmented prompt (Architectural Win)
            val segmentedContent = StringBuilder("Extract the canonical product data from the following categorized OCR text:\n")
            categorizedText.forEach { (panel, text) ->
                segmentedContent.append("**${panel.name} PANEL:** ${text.take(1000)}\n")
            }
            segmentedContent.append("\nReturn ONLY a raw JSON object with keys: brand, productName, category, size, ingredients, claims, directions.")

            val prompt = segmentedContent.toString()
            Log.d("LocalAiEngine", "Standardizing categorized text...")

            // Execution delegated to System NPU via Edge SDK
            val response = localModel.generateContent(prompt)
            val jsonText = response.text ?: return@withContext Result.failure(Exception("Empty AI response"))
            Log.d("LocalAiEngine", "Raw AI Response: $jsonText")

            // Safe JSON Extraction via Regex (Bypassing LLM markdown artifacts)
            val jsonRegex = Regex("""\{[\s\S]*\}""")
            val match = jsonRegex.find(jsonText) ?: return@withContext Result.failure(Exception("No JSON found in response"))
            val fullJson = match.value

            val result = json.decodeFromString<LocalStandardizedData>(fullJson)
            val duration = System.currentTimeMillis() - startTime
            Log.d("LocalAiEngine", "Standardization complete in ${duration}ms. Brand: ${result.brand}")

            Result.success(result)
        } catch (e: Exception) {
            Log.w("LocalAiEngine", "Local AI hardware bypass active: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Standardizes raw OCR text using Gemini Nano on-device.
     * Implements silent hardware bypass via Result.failure.
     */
    suspend fun standardizeOcrText(rawText: String): Result<LocalStandardizedData> = withContext(Dispatchers.Default) {
        // Delegate to categorized method with a generic FRONT panel for backward compatibility
        standardizeCategorizedText(mapOf(BoxPanel.FRONT to rawText))
    }
}
