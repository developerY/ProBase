package com.zoewave.probase.features.ai.firebase

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.generationConfig
import com.zoewave.probase.core.data.repository.AiConfigurationSettings
import com.zoewave.probase.features.ai.firebase.models.StyleTelemetry
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

data class AiResponse(
    val text: String,
    val promptTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int,
    val modelName: String
)

interface FirebaseAiClient {
    /**
     * Enforced Architectural Boundary: Only accepts StyleTelemetry.
     * Prevents raw appearance data from ever leaving the device.
     */
    suspend fun getStyleAdvice(telemetry: StyleTelemetry): AiResponse

    /**
     * Estimates the number of tokens for a given prompt without executing the model.
     */
    suspend fun estimateTokens(telemetry: StyleTelemetry): Int

    /**
     * Executes a raw prompt with optional temperature and model name overrides.
     */
    suspend fun generateContent(
        prompt: String,
        temperatureOverride: Float? = null,
        modelNameOverride: String? = null
    ): AiResponse

    /**
     * Estimates tokens for a raw prompt.
     */
    suspend fun countTokens(prompt: String): Int
}

@Singleton
class FirebaseAiClientImpl @Inject constructor(
    private val aiSettings: AiConfigurationSettings
) : FirebaseAiClient {

    private suspend fun resolveModelName(override: String?): String {
        if (!override.isNullOrBlank()) {
            Log.d("FirebaseAiClient", "Model Name Resolved from local override: $override")
            return override
        }
        return try {
            val flowModel = aiSettings.aiModelFlow.firstOrNull()?.ifBlank { null }
            if (flowModel != null) {
                Log.d("FirebaseAiClient", "Model Name Resolved from Settings Flow: $flowModel")
                flowModel
            } else {
                Log.d("FirebaseAiClient", "Model Name Missing from Settings. Using safe fallback: gemini-1.5-flash")
                "gemini-1.5-flash"
            }
        } catch (e: Exception) {
            Log.e("FirebaseAiClient", "Error resolving AI Model from flow, forcing fallback gemini-1.5-flash", e)
            "gemini-1.5-flash"
        }
    }

    private suspend fun getModel(
        temperatureOverride: Float? = null,
        modelNameOverride: String? = null
    ): GenerativeModel {
        val modelName = resolveModelName(modelNameOverride)
        Log.d("FirebaseAiClient", "Initializing GenerativeModel with dynamic modelName: $modelName")
        return Firebase.ai(
            backend = GenerativeBackend.googleAI(),
            useLimitedUseAppCheckTokens = false
        ).generativeModel(
            modelName = modelName,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                temperature = temperatureOverride ?: 0.75f
            }
        )
    }

    private fun buildPrompt(telemetry: StyleTelemetry): String {
        return """
            You are the KoColor Style Engine. Analyze the following Appearance and Context:
            
            APPEARANCE:
            - Temperature: ${telemetry.appearance.temperature}
            - Depth: ${telemetry.appearance.depth}
            - Contrast: ${telemetry.appearance.contrast}
            
            CONTEXT:
            - Weather: ${telemetry.weatherContext}
            - Circadian: ${telemetry.circadianContext}
            
            AVAILABLE VAULT (MATRIX REPRESENTATION):
            ${telemetry.vaultManifest}
            
            GOAL:
            1. Select BEST 3 clothing items (Top, Bottom, Shoes) from the wardrobe section of the manifest. 
            2. Select exactly 4 PIGMENT makeup items (1 Eye, 1 Cheek, 1 Lip, 1 Nail) strictly from the cosmetics section of the manifest.
            3. Select 1-2 DEFENSIVE items (Complexion/Skincare) from the cosmetics section based strictly on the WEATHER context.
            4. Create a 4-color Palette (HEX codes) harmonizing the whole look.
            
            CRITICAL SYNTAX RULE: When referencing ANY selected item in your rationale, you MUST use the exact inline tag format <ITEM:id>.
            EXAMPLE RATIONALE: "The <ITEM:w_55> creates a warm focal point against the cool <ITEM:w_38>, while the <ITEM:c_151> keeps the palette grounded."
            
            Provide a styling blueprint optimized for this context.
            
            Respond ONLY with a valid JSON object matching this schema:
            {
              "rationale": "string",
              "selectedClothingIds": ["w_id", "w_id", "w_id"],
              "selectedCosmeticIds": ["c_id", "c_id", "c_id", "c_id", ...],
              "recommendedPalette": ["#HEX", "#HEX", "#HEX", "#HEX"]
            }
        """.trimIndent()
    }

    override suspend fun estimateTokens(telemetry: StyleTelemetry): Int {
        val prompt = buildPrompt(telemetry)
        return getModel().countTokens(prompt).totalTokens
    }

    override suspend fun getStyleAdvice(telemetry: StyleTelemetry): AiResponse {
        val prompt = buildPrompt(telemetry)
        return generateContent(prompt)
    }

    override suspend fun countTokens(prompt: String): Int {
        return getModel().countTokens(prompt).totalTokens
    }

    override suspend fun generateContent(
        prompt: String,
        temperatureOverride: Float?,
        modelNameOverride: String?
    ): AiResponse {
        val modelName = resolveModelName(modelNameOverride)
        Log.d("KoColorAI_IO", ">>> REQUEST TO GEMINI (RAW, Model=$modelName):\n$prompt")
        
        return try {
            val model = getModel(temperatureOverride, modelNameOverride)
            val response = model.generateContent(prompt)
            
            Log.d("KoColorAI_IO", "^^^ RESPONSE FROM GEMINI (RAW):\n${response.text ?: "EMPTY_RESPONSE"}")
            
            val metadata = response.usageMetadata
            if (metadata != null) {
                Log.d("KoColorAI_IO", "TOKEN USAGE: Prompt=${metadata.promptTokenCount}, Candidates=${metadata.candidatesTokenCount}, Total=${metadata.totalTokenCount}")
            }
            
            val text = response.text ?: throw IllegalStateException("Empty response from Firebase AI Logic")
            
            AiResponse(
                text = text,
                promptTokens = metadata?.promptTokenCount ?: 0,
                completionTokens = metadata?.candidatesTokenCount ?: 0,
                totalTokens = metadata?.totalTokenCount ?: 0,
                modelName = modelName
            )
        } catch (e: Exception) {
            Log.e("KoColorAI_IO", "FATAL EXCEPTION DURING CLOUD GENERATION CALL!", e)
            throw e
        }
    }
}
