package com.zoewave.probase.features.ai.vision.receipt

import android.graphics.Bitmap
import android.util.Log
import com.zoewave.probase.features.ai.vision.receipt.data.CloudReceiptEngine
import com.zoewave.probase.features.ai.vision.receipt.data.LocalReceiptEngine
import com.zoewave.probase.features.compliance.AgeSignalsManager
import com.zoewave.probase.features.compliance.model.AgeRange
import com.zoewave.probase.features.compliance.model.ComplianceError
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class ReceiptOrchestrator @Inject constructor(
    @Named("CloudReceipt") private val cloudEngine: ReceiptEngine,
    @Named("LocalReceipt") private val localEngine: ReceiptEngine,
    private val ageSignalsManager: AgeSignalsManager
) {
    private val tag = "ReceiptOrchestrator"

    suspend fun processReceipt(
        bitmap: Bitmap,
        apiKey: String?,
        modelName: String? = null,
        userContext: String? = null
    ): ReceiptDiagnosticResult {
        val totalLogs = mutableListOf<String>()
        return try {
            if (!apiKey.isNullOrBlank()) {
                /*
                Log.d(tag, "Performing Compliance Handshake...")
                totalLogs.add("Orchestrator: Compliance check...")
                
                val ageSignalResult = ageSignalsManager.getAgeSignal()
                val isAllowed = ageSignalResult.fold(
                    onSuccess = { signal ->
                        signal.isAuthorizedForCloudAI
                    },
                    onFailure = { error ->
                        Log.e(tag, "Compliance error: ${error.message}")
                        false
                    }
                )

                if (!isAllowed) {
                    totalLogs.add("Orchestrator: Compliance restricted. Using Local AI.")
                    val result = localEngine.processReceipt(bitmap, null, userContext = userContext)
                    return result.copy(
                        logs = totalLogs + result.logs,
                        warnings = listOf("Cloud analysis restricted for compliance. Using local extraction.")
                    )
                }
                */

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
