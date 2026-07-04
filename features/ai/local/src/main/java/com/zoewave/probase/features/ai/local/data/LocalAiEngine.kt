package com.zoewave.probase.features.ai.local.data

import android.content.Context
import android.util.Log
import com.google.ai.edge.aicore.GenerativeModel
import com.google.ai.edge.aicore.generationConfig
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
     * Uses on-device LLM to standardize raw OCR text.
     * Implements silent hardware bypass via Result.failure.
     */
    suspend fun standardizeOcrText(rawText: String): Result<LocalStandardizedData> = withContext(Dispatchers.Default) {
        if (rawText.isBlank()) return@withContext Result.success(LocalStandardizedData())

        val startTime = System.currentTimeMillis()
        try {
            // Hardware Capability Check (Master Prompt Rule)
            val capability = checkCapability()
            Log.d("LocalAiEngine", "Checking capability: $capability")
            if (capability != NanoState.Available) {
                return@withContext Result.failure(UnsupportedOperationException("Gemini Nano not available: $capability"))
            }

            // Context Protection: Truncate to prevent token overflow
            Log.d("LocalAiEngine", "INCOMING RAW OCR TEXT:\n$rawText")
            val safeRawText = rawText.take(4000)
            Log.d("LocalAiEngine", "Standardizing ${safeRawText.length} chars of OCR text...")

            val prompt = """
                Standardize the following cosmetic product OCR text into JSON.
                Identify the Brand, Product Name, Category, Size, Ingredients, Claims, and Directions.
                Return ONLY a raw JSON object with those keys.
                If any field is missing, use null.
                
                OCR TEXT:
                $safeRawText
            """.trimIndent()

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
            // SILENT BYPASS: Gracefully return failure for progressive enhancement fallback
            Log.w("LocalAiEngine", "Local AI hardware bypass active: ${e.message}")
            Result.failure(e)
        }
    }
}
