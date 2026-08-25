package com.zoewave.probase.features.ai.firebase

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.generationConfig
import com.zoewave.probase.features.ai.firebase.models.StyleTelemetry
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
     * Enforced Architectural Boundary: Only accepts StyleTelemetry and intent string.
     * Prevents raw appearance data from ever leaving the device.
     */
    suspend fun getStyleAdvice(telemetry: StyleTelemetry, intent: String): AiResponse

    /**
     * Estimates the number of tokens for a given prompt without executing the model.
     */
    suspend fun estimateTokens(telemetry: StyleTelemetry, intent: String): Int
}

@Singleton
class FirebaseAiClientImpl @Inject constructor() : FirebaseAiClient {

    private val MODEL_NAME = "gemini-3.5-flash-lite"
    
    private fun getModel() = Firebase.ai(
        backend = GenerativeBackend.googleAI(),
        useLimitedUseAppCheckTokens = true
    ).generativeModel(
        modelName = MODEL_NAME,
        generationConfig = generationConfig {
            responseMimeType = "application/json"
        }
    )

    private fun buildPrompt(telemetry: StyleTelemetry, intent: String): String {
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
            EXAMPLE RATIONALE: "The <ITEM:w_55> is selected because the weather requires <ITEM:c_151>."
            
            Provide a styling blueprint optimized for this intent: "$intent".
            
            Respond ONLY with a valid JSON object matching this schema:
            {
              "rationale": "string",
              "selectedClothingIds": ["w_id", "w_id", "w_id"],
              "selectedCosmeticIds": ["c_id", "c_id", "c_id", "c_id", ...],
              "recommendedPalette": ["#HEX", "#HEX", "#HEX", "#HEX"]
            }
        """.trimIndent()
    }

    override suspend fun estimateTokens(telemetry: StyleTelemetry, intent: String): Int {
        val prompt = buildPrompt(telemetry, intent)
        return getModel().countTokens(prompt).totalTokens
    }

    override suspend fun getStyleAdvice(telemetry: StyleTelemetry, intent: String): AiResponse {
        val prompt = buildPrompt(telemetry, intent)

        // 4. Execute Tier 0 Cloud Request with Logging
        Log.d("KoColorAI_IO", ">>> REQUEST TO GEMINI:\n$prompt")
        
        val response = getModel().generateContent(prompt)
        
        Log.d("KoColorAI_IO", "^^^ RESPONSE FROM GEMINI:\n${response.text ?: "EMPTY_RESPONSE"}")
        
        val metadata = response.usageMetadata
        if (metadata != null) {
            Log.d("KoColorAI_IO", "TOKEN USAGE: Prompt=${metadata.promptTokenCount}, Candidates=${metadata.candidatesTokenCount}, Total=${metadata.totalTokenCount}")
        }
        
        val text = response.text ?: throw IllegalStateException("Empty response from Firebase AI Logic")
        
        return AiResponse(
            text = text,
            promptTokens = metadata?.promptTokenCount ?: 0,
            completionTokens = metadata?.candidatesTokenCount ?: 0,
            totalTokens = metadata?.totalTokenCount ?: 0,
            modelName = MODEL_NAME
        )
    }
}

