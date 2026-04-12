package com.zoewave.probase.features.smartcapture.data

import com.google.ai.client.generativeai.GenerativeModel
import com.zoewave.probase.features.smartcapture.domain.SmartTask
import kotlinx.serialization.json.Json
import javax.inject.Inject

class GeminiNanoParser @Inject constructor() {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-nano",
        apiKey = "" // Gemini Nano on-device usually doesn't require a key via AICore
    )

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun parse(text: String): SmartTask? {
        return try {
            val prompt = """
                Parse the following text into a structured task. 
                Extract: title, description, due date (if any), estimated budget (if any), and a suggested category.
                Return ONLY a valid JSON object with keys: title, description, dueDate, estimatedBudget, suggestedCategory.
                Text: $text
            """.trimIndent()

            val response = generativeModel.generateContent(prompt)
            val jsonText = response.text ?: return null
            
            val cleanedJson = jsonText.substringAfter("{").substringBeforeLast("}")
            val fullJson = "{$cleanedJson}"
            
            json.decodeFromString<SmartTask>(fullJson).copy(rawText = text)
        } catch (e: Exception) {
            null
        }
    }
}
