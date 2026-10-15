package com.zoewave.probase.features.ai.capture.data

import android.graphics.Bitmap
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import com.zoewave.probase.core.model.tasks.SmartTaskDraft
import com.zoewave.probase.features.ai.capture.domain.DiagnosticResult
import com.zoewave.probase.features.ai.capture.domain.SmartCaptureEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject

@Serializable
private data class GeminiModelsResponse(val models: List<GeminiModelDto>)

@Serializable
private data class GeminiModelDto(val name: String, val supportedGenerationMethods: List<String>)

/**
 * Cloud Capture Engine implementation using Firebase AI.
 */
class CloudCaptureEngineImpl @Inject constructor() : SmartCaptureEngine {

    private val httpClient = OkHttpClient()

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    override suspend fun processImage(
        bitmap: Bitmap?,
        apiKey: String?,
        modelName: String?,
        userContext: String?,
        onLog: (String) -> Unit
    ): DiagnosticResult<SmartTaskDraft> = withContext(Dispatchers.IO) {
        val logs = mutableListOf("Cloud AI Engine initialized")
        onLog("Cloud Engine: Warming up...")

        if (bitmap == null && userContext.isNullOrBlank()) {
            logs.add("Error: No image or text provided")
            return@withContext DiagnosticResult(SmartTaskDraft(), logs, error = "No content to analyze", engineUsed = "Cloud AI")
        }

        val finalModelName = modelName ?: "gemini-3.8-flash"
        logs.add("Model: $finalModelName")
        if (!userContext.isNullOrBlank()) {
            logs.add("User Context provided: ${userContext.take(20)}...")
        }

        onLog("Preparing prompt for $finalModelName...")
        val generativeModel = Firebase.ai(
            backend = GenerativeBackend.googleAI(),
            useLimitedUseAppCheckTokens = false
        ).generativeModel(
            modelName = finalModelName,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
            }
        )

        val prompt = content {
            if (bitmap != null) {
                image(bitmap)
            }
            text("""
                You are a smart project management assistant. 
                ${if (bitmap != null) "From this image, deduce what the user is planning to do." else "The user has provided a text command for a new task."}
                ${if (!userContext.isNullOrBlank()) "Context/Command: '$userContext'" else ""}
                
                ${if (bitmap != null) "If the image is a broken object, a sketch, or a note, guess the average project requirements for this task." else ""}
                Extract the task information into a structured JSON format to autofill a ToDo app.
                
                Fields to find:
                - title: Actionable task name.
                - description: Summary.
                - category: HOME, WORK, HEALTH, FINANCE, PERSONAL.
                - priority: LOW, MEDIUM, HIGH, URGENT.
                - estimatedMinutes: Integer estimate.
                - tags: String list.
                
                Respond ONLY with a valid JSON object matching this schema.
            """.trimIndent())
        }

        try {
            onLog("Executing model request...")
            val response = generativeModel.generateContent(prompt)
            val jsonText = response.text?.replace("```json", "")?.replace("```", "")?.trim()
            if (jsonText != null) {
                val draft = json.decodeFromString<SmartTaskDraft>(jsonText)
                logs.add("Successfully parsed TaskDraft")
                DiagnosticResult(draft, logs, engineUsed = "Cloud AI ($finalModelName)")
            } else {
                DiagnosticResult(SmartTaskDraft(), logs, error = "Empty LLM output", engineUsed = "Cloud AI")
            }
        } catch (e: Exception) {
            logs.add("Error during generation: ${e.localizedMessage}")
            DiagnosticResult(SmartTaskDraft(), logs, error = e.localizedMessage, engineUsed = "Cloud AI")
        }
    }

    override suspend fun getAvailableModels(apiKey: String?): List<String> = withContext(Dispatchers.IO) {
        if (apiKey.isNullOrBlank()) return@withContext emptyList()

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models?key=$apiKey"
            val request = Request.Builder().url(url).build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()

                val bodyStr = response.body?.string() ?: return@withContext emptyList()
                val dto = json.decodeFromString<GeminiModelsResponse>(bodyStr)

                dto.models
                    .filter { it.supportedGenerationMethods.contains("generateContent") }
                    .map { it.name.replace("models/", "") }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun testModel(apiKey: String, modelName: String): String = withContext(Dispatchers.IO) {
        try {
            val model = Firebase.ai(
                backend = GenerativeBackend.googleAI(),
                useLimitedUseAppCheckTokens = false
            ).generativeModel(
                modelName = modelName
            )

            val response = model.generateContent("Ping! Respond with 'OK'.")
            if (response.text?.contains("OK", ignoreCase = true) == true || !response.text.isNullOrBlank()) {
                "Model '$modelName' is active and responsive!"
            } else {
                "Pinging model '$modelName' returned empty output."
            }
        } catch (e: Exception) {
            "Model test failed: ${e.localizedMessage}"
        }
    }
}
