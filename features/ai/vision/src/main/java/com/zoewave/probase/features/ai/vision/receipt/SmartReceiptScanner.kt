package com.zoewave.probase.features.ai.vision.receipt

import android.graphics.Bitmap
import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.zoewave.probase.features.readers.ocr.data.LocalOcrEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class SmartReceiptScanner(
    private val ocrEngine: LocalOcrEngine
) {

    // We use a separate model for Nano as requested, though API might vary by SDK version
    private val generativeModel = GenerativeModel(
        modelName = "gemini-nano",
        apiKey = "" // On-device Nano doesn't always need a key if managed by AICore
    )

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun scanReceipt(bitmap: Bitmap, userContext: String? = null): ReceiptResult = withContext(Dispatchers.Default) {
        
        // 1. ML Kit Text Extraction
        val visionText = ocrEngine.extractText(bitmap)

        if (visionText.isBlank() && userContext == null) return@withContext ReceiptResult()

        // 2. Try Gemini Nano
        try {
            // In a real production app, you'd check availability via AICore first.
            // For this implementation, we attempt and catch availability/unsupported errors as a fallback mechanism.
            val contextPrompt = userContext?.let { "\nUser provided context: $it" } ?: ""
            val prompt = """
                what is this a picture of?
                $contextPrompt
                Return ONLY a valid JSON object with keys: merchant, totalAmount, date, category, whatIsThis.
                Field 'whatIsThis' should contain a detailed description of what you see in the image.
                Text from image: $visionText
            """.trimIndent()

            Log.d("SmartReceiptScanner", "AI Prompt: $prompt")

            val inputContent = content {
                image(bitmap)
                text(prompt)
            }

            val response = generativeModel.generateContent(inputContent)
            val jsonText = response.text ?: throw Exception("Empty response")
            
            Log.d("SmartReceiptScanner", "AI Response: $jsonText")
            
            // Clean JSON string in case AI adds markdown wrappers
            val cleanedJson = jsonText.substringAfter("{").substringBeforeLast("}")
            val fullJson = "{$cleanedJson}"
            
            // Note: We'd normally map specific AI keys to our ReceiptResult
            // For brevity, we assume the AI follows the requested keys
            val result = json.decodeFromString<ReceiptResult>(fullJson)
            result
        } catch (_: Exception) {
            // 3. Fallback to Regex
            RegexReceiptParser.parse(visionText)
        }
    }
}
