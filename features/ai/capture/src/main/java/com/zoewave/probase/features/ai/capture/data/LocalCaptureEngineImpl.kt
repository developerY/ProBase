package com.zoewave.probase.features.ai.capture.data

import android.graphics.Bitmap
import com.zoewave.probase.core.model.tasks.SmartTaskDraft
import com.zoewave.probase.features.ai.capture.domain.DiagnosticResult
import com.zoewave.probase.features.ai.capture.domain.SmartCaptureEngine
import com.zoewave.probase.features.readers.ocr.data.LocalOcrEngine
import javax.inject.Inject

/**
 * Tier 2: Local Engine using ML Kit via standardized OCR module.
 * Extracts text and uses heuristics (Regex) to parse basic fields.
 */
class LocalCaptureEngineImpl @Inject constructor(
    private val ocrEngine: LocalOcrEngine
) : SmartCaptureEngine {

    override suspend fun processImage(
// ... (rest of the logic uses ocrEngine.extractText(bitmap))
        bitmap: Bitmap?,
        apiKey: String?,
        modelName: String?,
        userContext: String?,
        onLog: (String) -> Unit
    ): DiagnosticResult<SmartTaskDraft> {
        val logs = mutableListOf("Local AI Engine initialized")
        if (bitmap == null) {
            onLog("Local AI: No image to process.")
            logs.add("Error: Local AI requires an image for OCR analysis.")
            return DiagnosticResult(SmartTaskDraft(), logs, error = "Local AI requires an image.", engineUsed = "Local AI")
        }

        onLog("Initializing Local AI...")
        logs.add("Vision analysis started (ML Kit)")
        onLog("Running Vision OCR (ML Kit)...")
        
        val visionText = try {
            val result = ocrEngine.extractText(bitmap)
            logs.add("Local OCR successful: ${result.length} characters found")
            onLog("OCR successful! Analyzing content...")
            result
        } catch (e: Exception) {
            logs.add("Local OCR failed: ${e.message}")
            onLog("Local OCR failed: ${e.message}")
            ""
        }

        if (visionText.isBlank()) {
            onLog("No text found in image.")
            return DiagnosticResult(SmartTaskDraft(), logs, engineUsed = "Local AI")
        }

        onLog("Extracting fields with Regex...")
        val lines = visionText.lines().filter { it.isNotBlank() }
        val taskName = lines.firstOrNull()
        val budget = extractBudget(visionText)
        val dueDate = extractDate(visionText)
        
        logs.add("Regex extraction complete")
        onLog("Extraction complete.")

        return DiagnosticResult(
            draft = SmartTaskDraft(
                taskName = taskName,
                budget = budget,
                dueDate = dueDate
            ),
            logs = logs,
            engineUsed = "Local AI"
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
