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
        userContext: String? = null,
        forceLocalAi: Boolean = false
    ): ReceiptDiagnosticResult {
        val totalLogs = mutableListOf<String>()
        Log.d("SeaweedAI", "==================================================")
        Log.d("SeaweedAI", ">>> RECEIPT ORCHESTRATOR INITIATED <<<")
        Log.d("SeaweedAI", "Has BYOK API Key: ${!apiKey.isNullOrBlank()}")
        Log.d("SeaweedAI", "Force Local AI: $forceLocalAi")
        Log.d("SeaweedAI", "Model Preference: ${modelName ?: "Default (gemini-1.5-flash)"}")
        Log.d("SeaweedAI", "==================================================")

        return try {
            if (!forceLocalAi) {
                totalLogs.add("Orchestrator: Attempting Cloud Receipt AI (Firebase Vertex AI)")
                Log.d("SeaweedAI", "Orchestrator routing to Cloud Receipt Engine...")
                val result = cloudEngine.processReceipt(bitmap, apiKey, modelName, userContext)
                result.copy(logs = totalLogs + result.logs)
            } else {
                totalLogs.add("Orchestrator: Local AI selected, using Local Engine")
                Log.d("SeaweedAI", "Orchestrator routing to Local Receipt Engine...")
                val result = localEngine.processReceipt(bitmap, null, userContext = userContext)
                result.copy(logs = totalLogs + result.logs)
            }
        } catch (e: Exception) {
            Log.w("SeaweedAI", "Cloud engine failed (${e.localizedMessage}), falling back to Local AI")
            val fallbackLogs = listOf("Orchestrator: Cloud failed (${e.localizedMessage})", "Triggering Local AI Fallback")
            val result = localEngine.processReceipt(bitmap, null, userContext = userContext)
            result.copy(
                logs = totalLogs + fallbackLogs + result.logs,
                warnings = listOf("Cloud analysis unavailable. Using local extraction.")
            )
        }
    }
}
