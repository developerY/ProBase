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
 * Tier 1: Pro Engine using Gemini 1.5 Flash Cloud SDK.
 * Uses multimodal parsing to extract high-fidelity JSON.
 */
class CloudCaptureEngineImpl @Inject constructor() : SmartCaptureEngine {

    private val json = Json { 
        ignoreUnknownKeys = true 
        coerceInputValues = true
    }

    override suspend fun processImage(bitmap: Bitmap, apiKey: String?): DiagnosticResult {
        val logs = mutableListOf("Cloud Engine initialized")
        if (apiKey.isNullOrBlank()) {
            logs.add("Error: API Key is null or blank")
            throw IllegalArgumentException("Missing Gemini API Key for Pro Engine")
        }

        logs.add("API Key found (length: ${apiKey.length})")
        logs.add("Model: gemini-1.5-flash")

        val generativeModel = GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = apiKey,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
            }
        )

        val prompt = content {
            image(bitmap)
            text("""
                Extract task information from this image into a structured JSON format.
                Fields to find: 
                - category: A broad classification (e.g., Home, Work, Health).
                - projectName: If the task belongs to a specific project.
                - taskName: The main action or title.
                - duration: Estimated time required (e.g., 30m, 2h).
                - dueDate: Any visible deadlines.
                - budget: Any estimated costs (return as a number only).
                - subTasks: A list of smaller steps if visible.
                
                Ensure the JSON matches this schema exactly:
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
                return DiagnosticResult(SmartTaskDraft(), logs)
            }
            
            logs.add("Response received (${jsonText.length} chars)")
            
            // Clean JSON string in case of LLM artifacts
            val cleanedJson = jsonText.substringAfter("{").substringBeforeLast("}")
            val finalJson = "{$cleanedJson}"
            
            val draft = json.decodeFromString<SmartTaskDraft>(finalJson)
            logs.add("JSON decoding successful")
            DiagnosticResult(draft, logs)
        } catch (e: Exception) {
            logs.add("Gemini API call failed: ${e.message}")
            // Rethrow so the orchestrator knows to fallback
            throw e
        }
    }
}
