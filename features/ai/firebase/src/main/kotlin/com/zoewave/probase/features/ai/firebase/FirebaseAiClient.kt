package com.zoewave.probase.features.ai.firebase

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
     * Prevents raw biometric data from ever leaving the device.
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
            
            Provide a styling blueprint optimized for this intent: "$intent".
            
            Respond ONLY with a valid JSON object matching this schema:
            {
              "rationale": "string",
              "selectedClothingIds": ["w_id", "w_id", "w_id"],
              "selectedCosmeticIds": ["c_id", "c_id", "c_id", "c_id", ...],
              "recommendedPalette": ["#HEX", "#HEX", "#HEX", "#HEX"]
            }
        """.trimIndent()

        // 4. Execute Tier 0 Cloud Request
        val response = generativeModel.generateContent(prompt)
        return response.text ?: throw IllegalStateException("Empty response from Firebase AI Logic")
    }
}
