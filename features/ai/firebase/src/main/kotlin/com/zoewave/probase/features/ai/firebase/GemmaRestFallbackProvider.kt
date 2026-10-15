package com.zoewave.probase.features.ai.firebase

import android.util.Log
import com.zoewave.probase.core.data.repository.AiConfigurationSettings
import com.zoewave.probase.features.ai.core.AiInput
import com.zoewave.probase.features.ai.core.AiProvider
import com.zoewave.probase.features.ai.core.AiProviderCapability
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GemmaRestFallbackProvider @Inject constructor(
    private val aiSettings: AiConfigurationSettings
) : AiProvider {

    private val httpClient = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    private var activeModel: String = "gemma-4-26b-a4b-it"

    override val capability = AiProviderCapability(
        id = "gemma_4_cloud",
        displayName = "Gemma 4 Cloud (Free Tier)",
        maxInputTokens = 8192,
        maxOutputTokens = 2048,
        timeoutMillis = 15000L,
        maxCandidateAdditions = 25,
        minCandidateAdditions = 6,
        isLocal = false
    )

    fun setModelName(modelName: String) {
        if (modelName.isNotBlank()) {
            activeModel = modelName
        }
    }

    override suspend fun isAvailable(): Boolean {
        val apiKey = aiSettings.getGeminiApiKey()
        return !apiKey.isNullOrBlank()
    }

    override suspend fun countTokens(input: AiInput): Int {
        val promptStr = when (input) {
            is AiInput.TextOnly -> input.promptString
            is AiInput.Multimodal -> input.promptString
        }
        return (promptStr.length / 4).coerceAtLeast(100)
    }

    override suspend fun execute(input: AiInput): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = aiSettings.getGeminiApiKey()
        if (apiKey.isNullOrBlank()) {
            return@withContext Result.failure(IllegalStateException("API Key missing for Gemma 4 REST Provider"))
        }

        val promptStr = when (input) {
            is AiInput.TextOnly -> input.promptString
            is AiInput.Multimodal -> input.promptString
        }

        val targetModel = activeModel.ifBlank { "gemma-4-26b-a4b-it" }
        Log.d("Gemma4Rest", ">>> REQUEST TO HOSTED GEMMA 4 (Model=$targetModel):\n$promptStr")

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$targetModel:generateContent?key=$apiKey"

        val requestJson = buildJsonObject {
            putJsonArray("contents") {
                addJsonObject {
                    put("role", "user")
                    putJsonArray("parts") {
                        addJsonObject { put("text", promptStr) }
                    }
                }
            }
            putJsonObject("generationConfig") {
                put("temperature", input.temperatureOverride ?: 0.2f)
                put("responseMimeType", "application/json")
            }
        }.toString()

        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("x-goog-api-key", apiKey)
            .post(requestJson.toRequestBody("application/json".toMediaType()))
            .build()

        try {
            httpClient.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    Log.e("Gemma4Rest", "Gemma 4 REST Call Failed (${response.code}): $body")
                    return@withContext Result.failure(IllegalStateException("Gemma 4 REST API Failed (${response.code}): $body"))
                }

                val root = json.parseToJsonElement(body).jsonObject
                val candidates = root["candidates"]?.jsonArray
                val parts = candidates?.firstOrNull()?.jsonObject
                    ?.get("content")?.jsonObject
                    ?.get("parts")?.jsonArray

                val rawText = parts?.lastOrNull()?.jsonObject?.get("text")?.jsonPrimitive?.contentOrNull
                    ?: throw IllegalStateException("Empty response from Gemma 4 REST API")

                val cleanedText = rawText
                    .substringAfter("```json")
                    .substringAfter("```")
                    .substringBeforeLast("```")
                    .trim()

                Log.d("Gemma4Rest", "^^^ RESPONSE FROM GEMMA 4:\n$cleanedText")
                Result.success(cleanedText)
            }
        } catch (e: Exception) {
            Log.e("Gemma4Rest", "Exception executing Gemma 4 REST request", e)
            Result.failure(e)
        }
    }
}
