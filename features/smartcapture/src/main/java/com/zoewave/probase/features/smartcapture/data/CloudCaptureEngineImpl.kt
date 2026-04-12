package com.zoewave.probase.features.smartcapture.data

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.zoewave.probase.features.smartcapture.domain.SmartCaptureEngine
import com.zoewave.probase.features.smartcapture.domain.TaskDraftState
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

    override suspend fun processImage(bitmap: Bitmap, apiKey: String?): TaskDraftState {
        if (apiKey.isNullOrBlank()) throw IllegalArgumentException("Missing Gemini API Key for Pro Engine")

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
            val response = generativeModel.generateContent(prompt)
            val jsonText = response.text ?: return TaskDraftState()
            
            // Clean JSON string in case of LLM artifacts
            val cleanedJson = jsonText.substringAfter("{").substringBeforeLast("}")
            val finalJson = "{$cleanedJson}"
            
            json.decodeFromString<TaskDraftState>(finalJson)
        } catch (e: Exception) {
            // Rethrow so the orchestrator knows to fallback
            throw e
        }
    }
}
