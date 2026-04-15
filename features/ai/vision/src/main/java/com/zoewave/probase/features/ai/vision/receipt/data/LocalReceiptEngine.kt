package com.zoewave.probase.features.ai.vision.receipt.data

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.zoewave.probase.features.ai.vision.receipt.ReceiptDiagnosticResult
import com.zoewave.probase.features.ai.vision.receipt.ReceiptEngine
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class LocalReceiptEngine @Inject constructor() : ReceiptEngine {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override suspend fun processReceipt(
        bitmap: Bitmap,
        apiKey: String?,
        modelName: String?,
        userContext: String?
    ): ReceiptDiagnosticResult {
        val logs = mutableListOf("Local Receipt AI initialized")
        val image = InputImage.fromBitmap(bitmap, 0)
        
        val visionText = try {
            val result = recognizer.process(image).await().text
            logs.add("ML Kit OCR successful")
            result
        } catch (e: Exception) {
            logs.add("ML Kit OCR failed: ${e.message}")
            ""
        }

        if (visionText.isBlank()) return ReceiptDiagnosticResult(logs = logs, engineUsed = "Local AI")

        // 🚀 Minimalistic local extraction (Regex)
        val total = extractTotal(visionText)
        logs.add("Regex extraction complete")

        return ReceiptDiagnosticResult(
            total = total,
            logs = logs,
            engineUsed = "Local AI (Vision)"
        )
    }

    private fun extractTotal(text: String): Double? {
        val patterns = listOf(
            Regex("""Total[:\s]*\$?\s*(\d+\.\d{2})""", RegexOption.IGNORE_CASE),
            Regex("""Amount[:\s]*\$?\s*(\d+\.\d{2})""", RegexOption.IGNORE_CASE),
            Regex("""(\d+\.\d{2})""")
        )
        for (pattern in patterns) {
            val match = pattern.find(text)
            if (match != null) {
                return match.groupValues[1].toDoubleOrNull()
            }
        }
        return null
    }
}
