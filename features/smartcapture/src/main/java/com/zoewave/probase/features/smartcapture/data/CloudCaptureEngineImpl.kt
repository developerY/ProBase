package com.zoewave.probase.features.smartcapture.data

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.zoewave.probase.core.model.tasks.SmartTaskDraft
import com.zoewave.probase.features.smartcapture.domain.DiagnosticResult
import com.zoewave.probase.features.smartcapture.domain.SmartCaptureEngine
import kotlinx.serialization.json.Json
import javax.inject.Inject

/**
 * Tier 1: Pro Engine using Gemini 3.1 Flash Lite Cloud SDK.
 * Uses multimodal parsing to extract high-fidelity JSON.
 */
class CloudCaptureEngineImpl @Inject constructor() : SmartCaptureEngine {

    private val json = Json { 
        ignoreUnknownKeys = true 
        coerceInputValues = true
    }

    override suspend fun processImage(
        bitmap: Bitmap,
        apiKey: String?,
        modelName: String?
    ): DiagnosticResult {
        val logs = mutableListOf("Cloud AI Engine initialized")
        if (apiKey.isNullOrBlank()) {
            logs.add("Error: API Key is null or blank")
            throw IllegalArgumentException("Missing Gemini API Key for Pro Engine")
        }

        val finalModelName = modelName ?: "gemini-1.5-flash"
        logs.add("API Key found (length: ${apiKey.length})")
        logs.add("Model: $finalModelName")

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
