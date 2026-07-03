package com.zoewave.probase.features.readers.ocr.data

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalOcrEngine @Inject constructor() {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun extractText(bitmap: Bitmap): String {
        val image = InputImage.fromBitmap(bitmap, 0)
        return try {
            val result = recognizer.process(image).await()
            result.text
        } catch (e: Exception) {
            android.util.Log.e("LocalOcrEngine", "OCR processing failed", e)
            ""
        }
    }
    
    suspend fun extractTextFromBitmaps(bitmaps: List<Bitmap>): String {
        return bitmaps.map { extractText(it) }.joinToString("\n")
    }
}
