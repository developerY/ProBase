package com.zoewave.probase.features.ai.local.data

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class LocalStandardizedData(
    val brand: String? = null,
    val productName: String? = null,
    val ingredients: List<String> = emptyList()
)

@Singleton
class LocalAiEngine @Inject constructor() {

    // Model name for on-device Gemini Nano via AICore
    private val localModel = GenerativeModel(
        modelName = "gemini-nano",
        apiKey = "" // Handled by Android System/AICore on supported devices
    )

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Uses on-device LLM (Gemini Nano) to clean up and standardize raw OCR text.
     * Returns Result.failure(UnsupportedOperationException) if hardware bypass is needed.
     */
    suspend fun standardizeOcrText(rawText: String): Result<LocalStandardizedData> = withContext(Dispatchers.Default) {
        if (rawText.isBlank()) return@withContext Result.success(LocalStandardizedData())

        try {
            val prompt = """
                Extract and standardize the cosmetic product information from this messy OCR text.
                Identify the Brand, the full Product Name, and a list of key Ingredients.
                
                OCR TEXT:
                $rawText
                
                Return ONLY a raw JSON object with keys: brand, productName, ingredients.
                If any field is missing, use null.
            """.trimIndent()

            val response = localModel.generateContent(prompt)
            val jsonText = response.text ?: return@withContext Result.success(LocalStandardizedData())

            // Handle potential LLM markdown artifacts
            val cleanedJson = jsonText.substringAfter("{").substringBeforeLast("}")
            val fullJson = "{$cleanedJson}"

            Result.success(json.decodeFromString<LocalStandardizedData>(fullJson))
        } catch (e: Exception) {
            android.util.Log.w("LocalAiEngine", "Local AI hardware bypass triggered: ${e.message}")
            Result.failure(e)
        }
    }
}
