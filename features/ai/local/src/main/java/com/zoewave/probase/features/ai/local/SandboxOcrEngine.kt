package com.zoewave.probase.features.ai.local

import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

class SandboxOcrEngine {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun extractTextFromBitmap(bitmap: Bitmap): String {
        Log.d("AI_SANDBOX", "Start process bitmap: ${bitmap.width}x${bitmap.height}, config: ${bitmap.config}")
        return try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val result = recognizer.process(image).await()
            val text = result.text
            Log.d("AI_SANDBOX", "OCR process succeeded via visionkit pipeline. Length: ${text.length} chars")
            if (text.isNotEmpty()) {
                Log.d("AI_SANDBOX", "Raw OCR Snippet: ${text.take(100).replace("\n", " ")}...")
            } else {
                Log.w("AI_SANDBOX", "OCR succeeded but returned EMPTY text.")
            }
            text
        } catch (e: Exception) {
            Log.e("AI_SANDBOX", "OCR_FAILED: ${e.message}", e)
            "OCR_FAILED: ${e.message}"
        }
    }
}
