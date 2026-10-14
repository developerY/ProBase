package com.zoewave.probase.features.ai.firebase

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import com.zoewave.probase.core.data.repository.AiConfigurationSettings
import com.zoewave.probase.features.ai.firebase.models.StyleTelemetry
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

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
        var resolved = if (!override.isNullOrBlank()) {
            Log.d("FirebaseAiClient", "Model Name Resolved from local override: $override")
            override
        } else {
            try {
                val flowModel = aiSettings.aiModelFlow.firstOrNull()?.ifBlank { null }
                if (flowModel != null) {
                    Log.d("FirebaseAiClient", "Model Name Resolved from Settings Flow: $flowModel")
                    flowModel
                } else {
                    Log.d("FirebaseAiClient", "Model Name Missing from Settings. Using safe fallback: gemini-3.8-flash")
                    "gemini-3.8-flash"
                }
            } catch (e: Exception) {
                Log.e("FirebaseAiClient", "Error resolving AI Model from flow, forcing fallback gemini-3.8-flash", e)
                "gemini-3.8-flash"
            }
        }

        // Sanitizer / Migration Check for deprecated models
        if (resolved.startsWith("gemini-1.") || resolved.startsWith("gemini-2.")) {
            Log.w("FirebaseAiClient", "Deprecated model '$resolved' detected in Settings Flow. Auto-migrating to 'gemini-3.8-flash'")
            resolved = "gemini-3.8-flash"
        }

        return resolved
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
            systemInstruction = content {
                text("""
                    You are the KoColor Style Architect AI. Generate a "Style Blueprint" that is stylistically harmonic and appropriate to the supplied environmental context.
                    
                    STRICT GROUNDING RULES & CONSTRAINTS:
                    1. DESCRIPTIVE ACCURACY: Do not invent stylistic adjectives (e.g., do not call nylon 'structural'). Describe items strictly using the physical materials and attributes listed in the manifest.
                    2. CATEGORY ISOLATION: You may ONLY select cosmetics from the requested roles. You are STRICTLY FORBIDDEN from selecting or referencing items categorized as PREP, HAIR, or COMPLEXION, unless explicitly requested in the environmental context. You must only select clothing items from the TOPS, BOTTOMS, SHOES, or OUTERWEAR categories.
                    3. RATIONALE FORMATTING: Write the rationale as fluid prose. Do not invent qualitative weather adjectives (like "mild" or "breezy"). Use ONLY the provided context variables. Do not use decimals.
                    
                    Respond ONLY with a valid JSON object matching the requested schema.
                """.trimIndent())
            },
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = Schema.obj(
                    properties = mapOf(
                        "selectedClothingIds" to Schema.array(
                            Schema.string("Selected wardrobe item IDs, e.g., w_38")
                        ),
                        "selectedCosmeticIds" to Schema.array(
                            Schema.string("Selected cosmetic item IDs, e.g., c_61")
                        ),
                        "rationale" to Schema.string("Harmonic synthesis rationale referencing ONLY selected item IDs"),
                        "recommendedPalette" to Schema.array(
                            Schema.string("Hex color codes for recommended palette, e.g., #1E88E5")
                        )
                    ),
                    optionalProperties = listOf("recommendedPalette")
                )
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
        return countTokens(prompt)
    }

    override suspend fun getStyleAdvice(telemetry: StyleTelemetry): AiResponse {
        val prompt = buildPrompt(telemetry)
        return generateContent(prompt)
    }

    private fun isTemporaryOverloadError(e: Throwable): Boolean {
        val msg = e.message ?: ""
        return msg.contains("high demand", ignoreCase = true) ||
               msg.contains("503", ignoreCase = true) ||
               msg.contains("unavailable", ignoreCase = true) ||
               msg.contains("Resource exhausted", ignoreCase = true) ||
               msg.contains("RESOURCE_EXHAUSTED", ignoreCase = true) ||
               msg.contains("429", ignoreCase = true) ||
               msg.contains("rate limit", ignoreCase = true) ||
               msg.contains("overloaded", ignoreCase = true) ||
               e is java.net.SocketTimeoutException ||
               e is java.io.IOException
    }

    private fun isDeprecatedModelError(e: Throwable): Boolean {
        val msg = e.message ?: ""
        return msg.contains("is no longer available", ignoreCase = true) ||
               msg.contains("discontinued", ignoreCase = true)
    }

    private suspend fun <T> executeWithRetry(
        actionName: String,
        initialModelName: String,
        block: suspend (modelName: String) -> T
    ): T {
        val fallbackModels = listOf(initialModelName, "gemini-3.8-flash", "gemini-2.5-flash", "gemini-1.5-flash").distinct()
        var lastException: Throwable? = null

        for (model in fallbackModels) {
            var baseDelayMs = 1000L
            val maxAttemptsPerModel = 3

            for (attempt in 1..maxAttemptsPerModel) {
                try {
                    return block(model)
                } catch (e: Exception) {
                    lastException = e
                    val msg = e.message ?: ""

                    // 1. Deprecated Model Error -> Immediately switch to next fallback model
                    if (isDeprecatedModelError(e)) {
                        Log.w("FirebaseAiClient", "[$actionName] Model '$model' is retired/unavailable ($msg). Immediately falling over to next model.")
                        break
                    }

                    // 2. Temporary Server Overload / Rate Limit -> Exponential Backoff + Jitter
                    if (isTemporaryOverloadError(e) && attempt < maxAttemptsPerModel) {
                        val jitter = Random.nextLong(100, 500)
                        val totalDelay = baseDelayMs + jitter
                        Log.w(
                            "FirebaseAiClient",
                            "[$actionName] High demand / capacity error on model '$model' (Attempt $attempt/$maxAttemptsPerModel): $msg. Retrying in ${totalDelay}ms..."
                        )
                        delay(totalDelay)
                        baseDelayMs *= 2 // Exponential backoff: 1s -> 2s -> 4s
                        continue
                    }

                    // Non-retryable error or exhausted attempts for this model
                    Log.w("FirebaseAiClient", "[$actionName] Attempt $attempt/$maxAttemptsPerModel on model '$model' failed: $msg")
                    break
                }
            }
        }

        val finalException = lastException ?: IllegalStateException("AI execution failed across all models and retries.")
        val msg = finalException.message ?: ""

        if (msg.contains("App Check", ignoreCase = true) || msg.contains("invalid", ignoreCase = true) || msg.contains("403")) {
            Log.e(
                "FirebaseAiClient",
                "CRITICAL: Firebase App Check token is invalid or unverified ($msg). " +
                "ACTION REQUIRED: Copy the 'DebugAppCheckProvider' token from Logcat and add it to the Firebase Console under App Check -> Apps -> Manage Debug Tokens.",
                finalException
            )
        } else {
            Log.e("FirebaseAiClient", "FATAL: AI execution exhausted all retries and model fallbacks for $actionName", finalException)
        }

        throw finalException
    }

    override suspend fun countTokens(prompt: String): Int {
        val initialModel = resolveModelName(null)
        return try {
            executeWithRetry("countTokens", initialModel) { model ->
                getModel(modelNameOverride = model).countTokens(prompt).totalTokens
            }
        } catch (e: Exception) {
            Log.w("FirebaseAiClient", "countTokens failed across retries/models. Using heuristic fallback estimate.")
            (prompt.length / 4).coerceAtLeast(100)
        }
    }

    override suspend fun generateContent(
        prompt: String,
        temperatureOverride: Float?,
        modelNameOverride: String?
    ): AiResponse {
        val initialModelName = resolveModelName(modelNameOverride)
        Log.d("KoColorAI_IO", ">>> REQUEST TO GEMINI (RAW, Target Model=$initialModelName):\n$prompt")

        return executeWithRetry("generateContent", initialModelName) { currentModel ->
            val model = getModel(temperatureOverride, currentModel)
            val response = model.generateContent(prompt)

            Log.d("KoColorAI_IO", "^^^ RESPONSE FROM GEMINI (RAW, Model=$currentModel):\n${response.text ?: "EMPTY_RESPONSE"}")

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
                modelName = currentModel
            )
        }
    }
}
