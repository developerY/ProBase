package com.zoewave.probase.features.ai.local.data

import android.content.Context
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

        try {
            // Hardware Capability Check (Master Prompt Rule)
            if (checkCapability() != NanoState.Available) {
                return@withContext Result.failure(UnsupportedOperationException("Gemini Nano not available"))
            }

            // Context Protection: Truncate to prevent token overflow
            val safeRawText = rawText.take(4000)

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

            // Safe JSON Extraction via Regex (Bypassing LLM markdown artifacts)
            val jsonRegex = Regex("""\{[\s\S]*\}""")
            val match = jsonRegex.find(jsonText) ?: return@withContext Result.failure(Exception("No JSON found in response"))
            val fullJson = match.value

            Result.success(json.decodeFromString<LocalStandardizedData>(fullJson))
        } catch (e: Exception) {
            // SILENT BYPASS: Gracefully return failure for progressive enhancement fallback
            android.util.Log.w("LocalAiEngine", "Local AI hardware bypass active: ${e.message}")
            Result.failure(e)
        }
    }
}
