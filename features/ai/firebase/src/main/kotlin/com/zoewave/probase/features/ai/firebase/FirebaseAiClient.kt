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

interface FirebaseAiClient {
    /**
     * Enforced Architectural Boundary: Only accepts StyleTelemetry and intent string.
     * Prevents raw appearance data from ever leaving the device.
     */
    suspend fun getStyleAdvice(telemetry: StyleTelemetry, intent: String): String
}

@Singleton
class FirebaseAiClientImpl @Inject constructor() : FirebaseAiClient {
    
    override suspend fun getStyleAdvice(telemetry: StyleTelemetry, intent: String): String {
        // 1. Initialize Firebase AI Logic (Requires valid App Check token)
        val generativeModel = Firebase.ai(
            backend = GenerativeBackend.googleAI(),
            useLimitedUseAppCheckTokens = true
        ).generativeModel(
            modelName = "gemini-3.5-flash-lite",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
            }
        )

        // 2. Serialize the safe telemetry data for debug logging or if we wanted to pass raw json to prompt
        // val telemetryJson = Json.encodeToString(telemetry)

        // 3. Construct the deterministic prompt using the enforced boundary data
        val prompt = """
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

        // 4. Execute Tier 0 Cloud Request with Logging
        Log.d("KoColorAI_IO", ">>> REQUEST TO GEMINI:\n$prompt")
        
        val response = generativeModel.generateContent(prompt)
        
        Log.d("KoColorAI_IO", "^^^ RESPONSE FROM GEMINI:\n${response.text ?: "EMPTY_RESPONSE"}")
        
        response.usageMetadata?.let { metadata ->
            Log.d("KoColorAI_IO", "TOKEN USAGE: Prompt=${metadata.promptTokenCount}, Candidates=${metadata.candidatesTokenCount}, Total=${metadata.totalTokenCount}")
        }
        
        return response.text ?: throw IllegalStateException("Empty response from Firebase AI Logic")
    }
}
