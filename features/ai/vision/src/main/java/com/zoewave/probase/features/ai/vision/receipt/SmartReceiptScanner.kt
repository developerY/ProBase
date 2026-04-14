package com.zoewave.probase.features.ai.vision.receipt

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class SmartReceiptScanner {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    
    // We use a separate model for Nano as requested, though API might vary by SDK version
    private val generativeModel = GenerativeModel(
        modelName = "gemini-nano",
        apiKey = "" // On-device Nano doesn't always need a key if managed by AICore
    )

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun scanReceipt(bitmap: Bitmap): ReceiptResult = withContext(Dispatchers.Default) {
        val image = InputImage.fromBitmap(bitmap, 0)
        
        // 1. ML Kit Text Extraction
        val visionText = try {
            recognizer.process(image).await().text
        } catch (_: Exception) {
            ""
        }

        if (visionText.isBlank()) return@withContext ReceiptResult()

        // 2. Try Gemini Nano
        try {
            // In a real production app, you'd check availability via AICore first.
            // For this implementation, we attempt and catch availability/unsupported errors as a fallback mechanism.
            val prompt = """
                Extract the merchant name, total amount, and date from the following receipt text. 
                Suggest a category. 
                Return ONLY a valid JSON object with keys: merchant, total, date, category.
                Text: $visionText
            """.trimIndent()

            val response = generativeModel.generateContent(prompt)
            val jsonText = response.text ?: throw Exception("Empty response")
            
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
