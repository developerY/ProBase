package com.zoewave.probase.features.ai.vision.receipt

import android.graphics.Bitmap
import com.zoewave.probase.features.ai.vision.receipt.data.CloudReceiptEngine
import com.zoewave.probase.features.ai.vision.receipt.data.LocalReceiptEngine
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class ReceiptOrchestrator @Inject constructor(
    @Named("CloudReceipt") private val cloudEngine: ReceiptEngine,
    @Named("LocalReceipt") private val localEngine: ReceiptEngine
) {
    suspend fun processReceipt(
        bitmap: Bitmap,
        apiKey: String?,
        modelName: String? = null,
        userContext: String? = null
    ): ReceiptDiagnosticResult {
        val totalLogs = mutableListOf<String>()
        return try {
            if (!apiKey.isNullOrBlank()) {
                totalLogs.add("Orchestrator: Attempting Cloud Receipt AI")
                val result = cloudEngine.processReceipt(bitmap, apiKey, modelName, userContext)
                result.copy(logs = totalLogs + result.logs)
            } else {
                totalLogs.add("Orchestrator: No API key, using Local AI")
                val result = localEngine.processReceipt(bitmap, null, userContext = userContext)
                result.copy(logs = totalLogs + result.logs)
            }
        } catch (e: Exception) {
            val fallbackLogs = listOf("Orchestrator: Cloud failed (${e.localizedMessage})", "Triggering Local AI Fallback")
            val result = localEngine.processReceipt(bitmap, null, userContext = userContext)
            result.copy(
                logs = totalLogs + fallbackLogs + result.logs,
                warnings = listOf("Cloud analysis unavailable. Using local extraction.")
            )
        }
    }
}
