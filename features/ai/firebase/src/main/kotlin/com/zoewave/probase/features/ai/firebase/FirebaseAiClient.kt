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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withTimeout
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

        // Sanitizer / Migration Check for deprecated 1.x models only
        if (resolved.startsWith("gemini-1.")) {
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
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                temperature = temperatureOverride ?: 0.2f
            }
        )
    }

    private fun isAppCheckOrRateLimitError(e: Throwable): Boolean {
        val msg = e.message ?: ""
        return msg.contains("App Check", ignoreCase = true) ||
               msg.contains("invalid", ignoreCase = true) ||
               msg.contains("Too many attempts", ignoreCase = true) ||
               msg.contains("TOO_MANY_ATTEMPTS", ignoreCase = true)
    }

    private fun isTemporaryOverloadError(e: Throwable): Boolean {
        val msg = e.message ?: ""
        return e is kotlinx.coroutines.TimeoutCancellationException ||
               msg.contains("deadline", ignoreCase = true) ||
               msg.contains("timeout", ignoreCase = true) ||
               msg.contains("high demand", ignoreCase = true) ||
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

    private fun extractDetailedMessage(e: Throwable): String {
        val rootCause = e.cause ?: e
        val rawMsg = rootCause.message ?: e.message ?: e.javaClass.simpleName
        return if (rawMsg.contains("Something unexpected happened", ignoreCase = true)) {
            val causeMsg = e.cause?.localizedMessage ?: e.cause?.toString()
            if (!causeMsg.isNullOrBlank()) {
                "Firebase AI Error (${e.javaClass.simpleName}): $causeMsg"
            } else {
                "Firebase AI Network/Quota Error (${e.javaClass.simpleName}). Check App Check debug token or quota limits."
            }
        } else {
            "${e.javaClass.simpleName}: $rawMsg"
        }
    }

    private suspend fun <T> executeWithRetry(
        actionName: String,
        initialModelName: String,
        timeoutPerAttemptMs: Long = 25_000L,
        block: suspend (modelName: String) -> T
    ): T {
        val fallbackModels = listOf(initialModelName, "gemini-3.8-flash", "gemini-flash-lite-latest").distinct()
        var lastException: Throwable? = null

        for (model in fallbackModels) {
            var baseDelayMs = 1000L
            val maxAttemptsPerModel = 2

            for (attempt in 1..maxAttemptsPerModel) {
                try {
                    return withTimeout(timeoutPerAttemptMs) {
                        block(model)
                    }
                } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                    lastException = e
                    Log.w("FirebaseAiClient", "[$actionName] Model '$model' timed out after ${timeoutPerAttemptMs}ms. Immediately falling over to next model.")
                    break
                } catch (e: kotlinx.coroutines.CancellationException) {
                    throw e
                } catch (e: Exception) {
                    lastException = e
                    val detailedMsg = extractDetailedMessage(e)

                    // 0. App Check / Rate Limit -> Immediately Abort Retries
                    if (isAppCheckOrRateLimitError(e)) {
                        Log.e("FirebaseAiClient", "[$actionName] App Check validation or rate limit error on '$model': $detailedMsg. Aborting fallbacks to prevent lockout.")
                        throw IllegalStateException(detailedMsg, e)
                    }

                    // 1. Deprecated Model Error -> Immediately switch to next fallback model
                    if (isDeprecatedModelError(e)) {
                        Log.w("FirebaseAiClient", "[$actionName] Model '$model' is retired/unavailable ($detailedMsg). Immediately falling over to next model.")
                        break
                    }

                    // 2. Temporary Server Overload / Rate Limit -> Exponential Backoff + Jitter
                    if (isTemporaryOverloadError(e) && attempt < maxAttemptsPerModel) {
                        val jitter = Random.nextLong(100, 500)
                        val totalDelay = baseDelayMs + jitter
                        Log.w(
                            "FirebaseAiClient",
                            "[$actionName] High demand / capacity error on model '$model' (Attempt $attempt/$maxAttemptsPerModel): $detailedMsg. Retrying in ${totalDelay}ms..."
                        )
                        delay(totalDelay)
                        baseDelayMs *= 2 // Exponential backoff: 1s -> 2s -> 4s
                        continue
                    }

                    // Non-retryable error or exhausted attempts for this model
                    Log.w("FirebaseAiClient", "[$actionName] Attempt $attempt/$maxAttemptsPerModel on model '$model' failed: $detailedMsg")
                    break
                }
            }
        }

        val rawException = lastException ?: IllegalStateException("AI execution failed across all models and retries.")
        val detailedStr = extractDetailedMessage(rawException)

        Log.e("FirebaseAiClient", "FATAL: AI execution exhausted all retries and model fallbacks for $actionName: $detailedStr", rawException)

        throw IllegalStateException(detailedStr, rawException)
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
        if (BuildConfig.DEBUG) {
            Log.d("KoColorAI_IO", ">>> REQUEST TO GEMINI (RAW, Target Model=$initialModelName):\n$prompt")
        }

        return executeWithRetry("generateContent", initialModelName) { currentModel ->
            val model = getModel(temperatureOverride, currentModel)
            val response = model.generateContent(prompt)
            
            val metadata = response.usageMetadata

            if (BuildConfig.DEBUG) {
                Log.d("KoColorAI_IO", "^^^ RESPONSE FROM GEMINI (RAW, Model=$currentModel):\n${response.text ?: "EMPTY_RESPONSE"}")
                if (metadata != null) {
                    Log.d("KoColorAI_IO", "TOKEN USAGE: Prompt=${metadata.promptTokenCount}, Candidates=${metadata.candidatesTokenCount}, Total=${metadata.totalTokenCount}")
                }
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
