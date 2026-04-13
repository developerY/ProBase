package com.zoewave.probase.features.smartcapture.data

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.zoewave.probase.core.model.tasks.SmartTaskDraft
import com.zoewave.probase.features.smartcapture.domain.SmartCaptureEngine
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Tier 2: Local Engine using Google ML Kit.
 * Extracts text and uses heuristics (Regex) to parse basic fields.
 */
class LocalCaptureEngineImpl @Inject constructor() : SmartCaptureEngine {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override suspend fun processImage(bitmap: Bitmap, apiKey: String?): SmartTaskDraft {
        val image = InputImage.fromBitmap(bitmap, 0)
        val visionText = try {
            recognizer.process(image).await().text
        } catch (e: Exception) {
            ""
        }

        if (visionText.isBlank()) return SmartTaskDraft()

        val lines = visionText.lines().filter { it.isNotBlank() }
        val taskName = lines.firstOrNull()
        val budget = extractBudget(visionText)
        val dueDate = extractDate(visionText)

        return SmartTaskDraft(
            taskName = taskName,
            budget = budget,
            dueDate = dueDate
        )
    }

    private fun extractBudget(text: String): Double? {
        val regex = Regex("""\$?\s?(\d+([.,]\d{2})?)""")
        return regex.find(text)?.groupValues?.get(1)?.replace(",", ".")?.toDoubleOrNull()
    }

    private fun extractDate(text: String): String? {
        // Look for common patterns like MM/DD/YYYY or YYYY-MM-DD
        val regex = Regex("""(\d{1,4}[/-]\d{1,2}[/-]\d{1,4})""")
        return regex.find(text)?.value
    }
}
