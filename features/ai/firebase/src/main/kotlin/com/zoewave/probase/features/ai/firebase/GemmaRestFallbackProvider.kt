package com.zoewave.probase.features.ai.firebase

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.auth.FirebaseAuth
import com.zoewave.probase.core.data.repository.AiConfigurationSettings
import com.zoewave.probase.features.ai.core.AiInput
import com.zoewave.probase.features.ai.core.AiProvider
import com.zoewave.probase.features.ai.core.AiProviderCapability
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
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
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GemmaRestFallbackProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val aiSettings: AiConfigurationSettings
) : AiProvider {

    companion object {
        private const val DEFAULT_PROXY_URL = "https://kocolor-gemma-proxy.biologica.workers.dev/"
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(180, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true }

    private var activeModel: String = "gemma-4-26b-a4b-it"

    override val capability = AiProviderCapability(
        id = "gemma_4_cloud",
        displayName = "Gemma 4 Cloud (Free Tier)",
        maxInputTokens = 8192,
        maxOutputTokens = 2048,
        timeoutMillis = 60000L,
        maxCandidateAdditions = 25,
        minCandidateAdditions = 6,
        isLocal = false
    )

    fun setModelName(modelName: String) {
        if (modelName.isNotBlank()) {
            activeModel = modelName
        }
    }

    private fun resolveGemmaApiKey(): String {
        return aiSettings.getGeminiApiKey() ?: "PROXY_ACTIVE_NO_LOCAL_KEY"
    }

    override suspend fun isAvailable(): Boolean {
        return resolveGemmaApiKey().isNotBlank()
    }

    override suspend fun countTokens(input: AiInput): Int {
        val promptStr = when (input) {
            is AiInput.TextOnly -> input.promptString
            is AiInput.Multimodal -> input.promptString
        }
        return (promptStr.length / 4).coerceAtLeast(100)
    }

    override suspend fun execute(input: AiInput): Result<String> = withContext(Dispatchers.IO) {
        val userKey = aiSettings.getGeminiApiKey()
        val targetModel = activeModel.ifBlank { "gemma-4-26b-a4b-it" }

        val basePromptStr = when (input) {
            is AiInput.TextOnly -> input.promptString
            is AiInput.Multimodal -> input.promptString
        }

        val promptStr = if (!basePromptStr.contains("CRITICAL OUTPUT FORMAT:")) {
            """
                $basePromptStr
                
                CRITICAL OUTPUT FORMAT:
                Do NOT use Markdown headings or bullet points. Respond ONLY with a single raw, valid JSON object matching this exact structure:
                {
                  "selectedClothingIds": ["w_1", "w_2", "w_3"],
                  "selectedCosmeticIds": ["c_1", "c_2", "c_3", "c_4"],
                  "rationale": "Single paragraph referencing only the selected item IDs.",
                  "recommendedPalette": ["#HEX1", "#HEX2", "#HEX3"]
                }
            """.trimIndent()
        } else {
            basePromptStr
        }

        if (!userKey.isNullOrBlank()) {
            return@withContext executeDirectApi(userKey, targetModel, promptStr, input.temperatureOverride)
        }

        executeProxyApi(targetModel, promptStr, input.temperatureOverride)
    }

    private suspend fun executeProxyApi(
        modelName: String,
        promptStr: String,
        temperatureOverride: Float?
    ): Result<String> {
        return try {
            val appCheckToken = try {
                FirebaseAppCheck.getInstance().getAppCheckToken(false).await().token
            } catch (e: Exception) {
                Log.w("Gemma4Rest", "Failed to acquire AppCheck token", e)
                ""
            }

            val auth = FirebaseAuth.getInstance()
            if (auth.currentUser == null) {
                try {
                    auth.signInAnonymously().await()
                } catch (e: Exception) {
                    Log.w("Gemma4Rest", "Failed anonymous sign in", e)
                }
            }

            val idToken = try {
                auth.currentUser?.getIdToken(false)?.await()?.token ?: ""
            } catch (e: Exception) {
                Log.w("Gemma4Rest", "Failed to acquire Auth ID token", e)
                ""
            }

            val requestJson = buildJsonObject {
                put("model", modelName)
                put("prompt", promptStr)
            }.toString()

            val requestBuilder = Request.Builder()
                .url(DEFAULT_PROXY_URL)
                .addHeader("Content-Type", "application/json")

            if (appCheckToken.isNotBlank()) {
                requestBuilder.addHeader("X-Firebase-AppCheck", appCheckToken)
            }
            if (idToken.isNotBlank()) {
                requestBuilder.addHeader("Authorization", "Bearer $idToken")
            }

            Log.d("Gemma4Rest", "Executing Keyless Proxy Request to $DEFAULT_PROXY_URL (Model=$modelName)")
            Log.d("Gemma4Rest", "Security Headers: AppCheck=${appCheckToken.isNotBlank()} (${appCheckToken.take(12)}...), AuthToken=${idToken.isNotBlank()}")

            val request = requestBuilder
                .post(requestJson.toRequestBody("application/json".toMediaType()))
                .build()

            val startTime = System.currentTimeMillis()
            httpClient.newCall(request).execute().use { response ->
                val duration = System.currentTimeMillis() - startTime
                val body = response.body?.string().orEmpty()

                Log.d("Gemma4Rest", "Proxy Response HTTP ${response.code} in ${duration}ms (Length: ${body.length} chars)")

                if (!response.isSuccessful) {
                    Log.e("Gemma4Rest", "Proxy Request Failed (${response.code}): $body")
                    return Result.failure(IllegalStateException("Proxy Failed (${response.code}): $body"))
                }

                parseGemmaResponse(body)
            }
        } catch (e: Exception) {
            Log.e("Gemma4Rest", "Proxy Exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun executeDirectApi(
        apiKey: String,
        targetModel: String,
        promptStr: String,
        temperatureOverride: Float?
    ): Result<String> {
        Log.d("Gemma4Rest", ">>> DIRECT REQUEST TO HOSTED GEMMA 4 (Model=$targetModel):\n$promptStr")

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
                put("temperature", temperatureOverride ?: 0.2f)
                putJsonObject("thinkingConfig") {
                    put("thinkingLevel", "MINIMAL")
                }
            }
        }.toString()

        val pkgName = context.packageName
        val sha1Cert = getSignatureSha1()

        val requestBuilder = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("x-goog-api-key", apiKey)

        if (pkgName.isNotBlank()) {
            requestBuilder.addHeader("X-Android-Package", pkgName)
        }
        if (sha1Cert.isNotBlank()) {
            requestBuilder.addHeader("X-Android-Cert", sha1Cert)
        }

        val request = requestBuilder
            .post(requestJson.toRequestBody("application/json".toMediaType()))
            .build()

        return try {
            httpClient.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    if (response.code == 403 && body.contains("API_KEY_SERVICE_BLOCKED")) {
                        val warnMsg = "Gemma 4 REST Direct Call blocked (403 API_KEY_SERVICE_BLOCKED). " +
                                "The API Key is restricted in Google Cloud Console. " +
                                "To resolve: Enable 'Generative Language API' on key in Google Cloud Console."
                        Log.w("Gemma4Rest", warnMsg)
                        return Result.failure(IllegalStateException(warnMsg))
                    }
                    Log.e("Gemma4Rest", "Gemma 4 REST Direct Call Failed (${response.code}): $body")
                    return Result.failure(IllegalStateException("Gemma 4 REST API Failed (${response.code}): $body"))
                }

                parseGemmaResponse(body)
            }
        } catch (e: Exception) {
            Log.e("Gemma4Rest", "Exception executing Gemma 4 REST direct request: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun parseGemmaResponse(body: String): Result<String> {
        val root = json.parseToJsonElement(body).jsonObject
        val candidates = root["candidates"]?.jsonArray
        val parts = candidates?.firstOrNull()?.jsonObject
            ?.get("content")?.jsonObject
            ?.get("parts")?.jsonArray

        if (parts == null || parts.isEmpty()) {
            return Result.failure(IllegalStateException("Empty response from Gemma 4 REST API"))
        }

        // 1. Collect ONLY non-thought parts (skip internal reasoning if sent back)
        val nonThoughtText = buildString {
            for (i in 0 until parts.size) {
                val partObj = parts[i].jsonObject
                val isThought = partObj["thought"]?.jsonPrimitive?.contentOrNull?.toBoolean() ?: false
                if (!isThought) {
                    append(partObj["text"]?.jsonPrimitive?.contentOrNull ?: "")
                }
            }
        }.trim()

        // 2. Strip ```json ... ``` markdown fences if present and isolate the JSON object '{ ... }'
        val cleaned = nonThoughtText
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        val firstBrace = cleaned.indexOf('{')
        val lastBrace = cleaned.lastIndexOf('}')
        
        val finalJson = if (firstBrace != -1 && lastBrace > firstBrace) {
            cleaned.substring(firstBrace, lastBrace + 1)
        } else {
            cleaned
        }

        Log.d("Gemma4Rest", "^^^ RESPONSE FROM GEMMA 4:\n$finalJson")
        return Result.success(finalJson)
    }

    private fun getSignatureSha1(): String {
        return try {
            val pm = context.packageManager
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                PackageManager.GET_SIGNING_CERTIFICATES
            } else {
                @Suppress("DEPRECATION")
                PackageManager.GET_SIGNATURES
            }
            val info = pm.getPackageInfo(context.packageName, flags)
            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                info.signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                info.signatures
            }
            val cert = signatures?.firstOrNull()?.toByteArray() ?: return ""
            val md = MessageDigest.getInstance("SHA-1")
            val digest = md.digest(cert)
            digest.joinToString("") { "%02X".format(it) }
        } catch (e: Exception) {
            Log.w("Gemma4Rest", "Failed to calculate SHA-1 cert fingerprint", e)
            ""
        }
    }
}
