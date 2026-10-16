package com.zoewave.probase.features.ai.firebase

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.google.firebase.FirebaseApp
import com.zoewave.probase.core.data.repository.AiConfigurationSettings
import com.zoewave.probase.features.ai.core.AiInput
import com.zoewave.probase.features.ai.core.AiProvider
import com.zoewave.probase.features.ai.core.AiProviderCapability
import dagger.hilt.android.qualifiers.ApplicationContext
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
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GemmaRestFallbackProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val aiSettings: AiConfigurationSettings
) : AiProvider {

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

    private fun resolveApiKey(): String {
        val userKey = aiSettings.getGeminiApiKey()
        if (!userKey.isNullOrBlank()) return userKey
        return runCatching {
            FirebaseApp.getInstance().options.apiKey
        }.getOrNull().orEmpty()
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

    override suspend fun isAvailable(): Boolean {
        return resolveApiKey().isNotBlank()
    }

    override suspend fun countTokens(input: AiInput): Int {
        val promptStr = when (input) {
            is AiInput.TextOnly -> input.promptString
            is AiInput.Multimodal -> input.promptString
        }
        return (promptStr.length / 4).coerceAtLeast(100)
    }

    override suspend fun execute(input: AiInput): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = resolveApiKey()
        if (apiKey.isBlank()) {
            return@withContext Result.failure(IllegalStateException("No valid API Key found for Gemma 4 REST Provider"))
        }

        val basePromptStr = when (input) {
            is AiInput.TextOnly -> input.promptString
            is AiInput.Multimodal -> input.promptString
        }

        val promptStr = """
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
            Log.e("Gemma4Rest", "Exception executing Gemma 4 REST request: ${e.message}", e)
            Result.failure(e)
        }
    }
}
