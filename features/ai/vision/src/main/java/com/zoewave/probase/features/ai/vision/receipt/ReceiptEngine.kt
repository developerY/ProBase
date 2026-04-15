package com.zoewave.probase.features.ai.vision.receipt

import android.graphics.Bitmap

/**
 * Interface for receipt parsing engines.
 */
interface ReceiptEngine {
    /**
     * Processes an image and attempts to parse it into a structured receipt.
     */
    suspend fun processReceipt(
        bitmap: Bitmap,
        apiKey: String?,
        modelName: String? = null,
        userContext: String? = null
    ): ReceiptDiagnosticResult
}

data class ReceiptDiagnosticResult(
    val merchant: String? = null,
    val total: Double? = null,
    val date: String? = null,
    val category: String? = null,
    val logs: List<String> = emptyList(),
    val engineUsed: String = "Unknown",
    val error: String? = null,
    val warnings: List<String> = emptyList()
)
