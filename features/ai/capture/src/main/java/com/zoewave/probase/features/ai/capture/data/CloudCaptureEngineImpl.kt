package com.zoewave.probase.features.ai.capture.data

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
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
 * Tier 1: Pro Engine using Gemini 3.1 Flash Lite Cloud SDK.
 * Uses multimodal parsing to extract high-fidelity JSON.
 */
class CloudCaptureEngineImpl @Inject constructor() : SmartCaptureEngine {

    private val httpClient = OkHttpClient()
    private val json = Json { 
        ignoreUnknownKeys = true 
        coerceInputValues = true
    }

    override suspend fun getAvailableModels(apiKey: String?): List<String> = withContext(Dispatchers.IO) {
        if (apiKey.isNullOrBlank()) return@withContext emptyList()
        
        val url = "https://generativelanguage.googleapis.com/v1beta/models?key=$apiKey"
        val request = Request.Builder().url(url).build()
        
        try {
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val body = response.body?.string() ?: return@withContext emptyList()
                val data = json.decodeFromString<GeminiModelsResponse>(body)
                
                data.models
                    .filter { it.supportedGenerationMethods.contains("generateContent") }
                    .map { it.name.removePrefix("models/") }
                    .filter { it.contains("gemini") }
                    .sorted()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun testModel(apiKey: String, modelName: String): String {
        val generativeModel = GenerativeModel(
            modelName = modelName,
            apiKey = apiKey
        )
        return try {
            val response = generativeModel.generateContent("What is your name and version?")
            response.text ?: "No response from model"
        } catch (e: Exception) {
            e.localizedMessage ?: "Connection Error"
        }
    }

    override suspend fun processImage(
        bitmap: Bitmap,
        apiKey: String?,
        modelName: String?,
        userContext: String?
    ): DiagnosticResult {
        val logs = mutableListOf("Cloud AI Engine initialized")
        if (apiKey.isNullOrBlank()) {
            logs.add("Error: API Key is null or blank")
            throw IllegalArgumentException("Missing Gemini API Key for Pro Engine")
        }

        val finalModelName = modelName ?: "gemini-1.5-flash"
        logs.add("API Key found (length: ${apiKey.length})")
        logs.add("Model: $finalModelName")
        if (!userContext.isNullOrBlank()) {
            logs.add("User Context provided: ${userContext.take(20)}...")
        }

        val generativeModel = GenerativeModel(
            modelName = finalModelName,
            apiKey = apiKey,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
            }
        )

        val prompt = content {
            image(bitmap)
            text("""
                You are a smart project management assistant. From this image, deduce what the user is planning to do.
                ${if (!userContext.isNullOrBlank()) "The user also provided this context: '$userContext'" else ""}
                If the image is a broken object, a sketch, or a note, guess the average project requirements for this task.
                Extract the task information into a structured JSON format to autofill a ToDo app.
                
                Fields to find: 
                - category: A broad classification (e.g., "Home", "Work", "Health").
                - projectName: The overarching project this belongs to, if applicable.
                - taskName: The main actionable title.
                - duration: Estimated time required (e.g., "30m", "2h").
                - dueDate: Any visible deadlines. Format as MM/DD/YYYY if possible.
                - budget: Any estimated costs. Return strictly as a primitive number without currency symbols (e.g., 15.50). Use null if none.
                - subTasks: A list of 3 to 5 logical smaller steps to complete the task.
                
                Respond ONLY with a valid JSON object matching this exact schema:
                {
                  "category": string or null,
                  "projectName": string or null,
                  "taskName": string or null,
                  "duration": string or null,
                  "dueDate": string or null,
                  "budget": number or null,
                  "subTasks": [string]
                }
            """.trimIndent())
        }

        return try {
            logs.add("Sending multimodal request to Gemini...")
            val response = generativeModel.generateContent(prompt)
            val jsonText = response.text ?: run {
                logs.add("Gemini returned empty response")
                return DiagnosticResult(SmartTaskDraft(), logs, engineUsed = "Cloud AI")
            }
            
            logs.add("Response received (${jsonText.length} chars)")
            
            // Clean JSON string in case of LLM artifacts
            val cleanedJson = jsonText.substringAfter("{").substringBeforeLast("}")
            val finalJson = "{$cleanedJson}"
            
            val draft = json.decodeFromString<SmartTaskDraft>(finalJson)
            logs.add("JSON decoding successful")
            DiagnosticResult(draft, logs, engineUsed = "Cloud AI")
        } catch (e: Exception) {
            logs.add("Gemini API call failed: ${e.message}")
            // Rethrow so the orchestrator knows to fallback
            throw e
        }
    }
}
