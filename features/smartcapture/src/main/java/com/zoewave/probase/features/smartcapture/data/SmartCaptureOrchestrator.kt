package com.zoewave.probase.features.smartcapture.data

import android.graphics.Bitmap
import android.util.Log
import com.zoewave.probase.core.model.tasks.SmartTaskDraft
import com.zoewave.probase.features.smartcapture.domain.DiagnosticResult
import com.zoewave.probase.features.smartcapture.domain.SmartCaptureEngine
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class SmartCaptureOrchestrator @Inject constructor(
    @Named("Cloud") private val cloudEngine: SmartCaptureEngine,
    @Named("Local") private val localEngine: SmartCaptureEngine
) {
    private val TAG = "SmartCaptureOrchestrator"

    suspend fun validateApiKey(apiKey: String, modelName: String): String {
        return try {
            val result = cloudEngine.processImage(
                bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888),
                apiKey = apiKey,
                modelName = modelName
            )
            "Valid! Connection successful."
        } catch (e: Exception) {
            e.localizedMessage ?: "Invalid Key or Connection Error"
        }
    }

    suspend fun processImage(
        bitmap: Bitmap,
        apiKey: String?,
        modelName: String? = null
    ): DiagnosticResult {
        val totalLogs = mutableListOf<String>()
        return try {
            if (!apiKey.isNullOrBlank()) {
                Log.d(TAG, "Attempting Tier 1 (Cloud) capture with $modelName...")
                totalLogs.add("Orchestrator: Cloud API Key present")
                val result = cloudEngine.processImage(bitmap, apiKey, modelName)
                result.copy(logs = totalLogs + result.logs)
            } else {
                Log.d(TAG, "No API key provided. Skipping Tier 1.")
                totalLogs.add("Orchestrator: No Cloud Key, using Local AI")
                val result = localEngine.processImage(bitmap, null)
                result.copy(logs = totalLogs + result.logs)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Tier 1 capture failed. Falling back to Tier 2 (Local).", e)
            val fallbackLogs = listOf("Orchestrator: Cloud failed (${e.localizedMessage})", "Triggering Local AI Fallback")
            val result = localEngine.processImage(bitmap, null)
            result.copy(
                logs = totalLogs + fallbackLogs + result.logs,
                warnings = listOf("Cloud analysis unavailable. Using local extraction.")
            )
        }
    }
}
