package com.zoewave.probase.features.ai.vision.receipt

import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import com.zoewave.probase.features.readers.ocr.data.LocalOcrEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class SmartReceiptScanner(
    private val ocrEngine: LocalOcrEngine
) {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun scanReceipt(bitmap: Bitmap, userContext: String? = null): ReceiptResult = withContext(Dispatchers.Default) {
        
        // 1. ML Kit Text Extraction
        val visionText = ocrEngine.extractText(bitmap)

        if (visionText.isBlank() && userContext == null) return@withContext ReceiptResult()

        // 2. Try Gemini Nano / Local AI via Firebase
        try {
            val generativeModel = Firebase.ai(
                backend = GenerativeBackend.googleAI()
            ).generativeModel(modelName = "gemini-1.5-flash")

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
            
            val result = json.decodeFromString<ReceiptResult>(fullJson)
            result
        } catch (_: Exception) {
            // 3. Fallback to Regex
            RegexReceiptParser.parse(visionText)
        }
    }
}
