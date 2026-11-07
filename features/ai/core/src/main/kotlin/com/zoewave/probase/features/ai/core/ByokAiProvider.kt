package com.zoewave.probase.features.ai.core

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.add
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ByokAiProvider @Inject constructor() : AiProvider {

    private var currentApiKey: String? = null
    private var currentModelName: String = "gemini-2.0-flash"
    private val httpClient = OkHttpClient()

    fun setApiKey(key: String?) {
        this.currentApiKey = key
    }

    fun setModelName(modelName: String?) {
        if (!modelName.isNullOrBlank()) {
            this.currentModelName = modelName
        }
    }

    override val capability = AiProviderCapability(
        id = "byok_cloud",
        displayName = "BYOK Cloud (Gemini API Key)",
        maxInputTokens = 8192,
        maxOutputTokens = 1024,
        timeoutMillis = 8000L,
        maxCandidateAdditions = 25,
        minCandidateAdditions = 6,
        isLocal = false
    )

    override suspend fun isAvailable(): Boolean {
        return !currentApiKey.isNullOrBlank()
    }

    override suspend fun countTokens(input: AiInput): Int {
        return input.promptString.length / 4
    }

    override suspend fun execute(input: AiInput): Result<String> = withContext(Dispatchers.IO) {
        if (input is AiInput.Multimodal) {
            return@withContext Result.failure(UnsupportedOperationException("BYOK Provider does not support multimodal inputs for privacy and token budget reasons"))
        }

        val apiKey = currentApiKey
        if (apiKey.isNullOrBlank()) {
            Log.w("ByokAiProvider", "API Key is null or blank")
            return@withContext Result.failure(Exception("BYOK Provider not configured"))
        }

        val normalizedModel = currentModelName.removePrefix("models/")
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$normalizedModel:generateContent?key=$apiKey"
        
                // Build JSON body securely using kotlinx.serialization
        val jsonBody = kotlinx.serialization.json.buildJsonObject {
            put("contents", kotlinx.serialization.json.buildJsonArray {
                add(kotlinx.serialization.json.buildJsonObject {
                    put("parts", kotlinx.serialization.json.buildJsonArray {
                        add(kotlinx.serialization.json.buildJsonObject {
                            put("text", input.promptString)
                        })
                    })
                })
            })
            // Enforce low temperature for reliability unless overriden
            val temp = input.temperatureOverride ?: 0.2f
            put("generationConfig", kotlinx.serialization.json.buildJsonObject {
                put("temperature", temp)
            })
        }.toString()
        
        val requestBody = jsonBody.toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        try {
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val err = response.body?.string() ?: ""
                    Log.e("ByokAiProvider", "HTTP Error ${response.code}: $err")
                    return@withContext Result.failure(Exception("HTTP Error ${response.code}"))
                }
                
                val bodyString = response.body?.string()
                if (bodyString.isNullOrBlank()) {
                    return@withContext Result.failure(Exception("Empty response body"))
                }

                // Parse the Gemini JSON response
                val json = Json.parseToJsonElement(bodyString).jsonObject
                val text = json["candidates"]
                    ?.jsonArray?.get(0)
                    ?.jsonObject?.get("content")
                    ?.jsonObject?.get("parts")
                    ?.jsonArray?.get(0)
                    ?.jsonObject?.get("text")
                    ?.jsonPrimitive?.contentOrNull
                    
                if (text != null) {
                    Log.d("ByokAiProvider", "Successfully executed prompt")
                    Result.success(text)
                } else {
                    Log.e("ByokAiProvider", "Failed to parse text from response: $bodyString")
                    Result.failure(Exception("Invalid response format"))
                }
            }
        } catch (e: Exception) {
            Log.e("ByokAiProvider", "Execution exception", e)
            Result.failure(e)
        }
    }
}
