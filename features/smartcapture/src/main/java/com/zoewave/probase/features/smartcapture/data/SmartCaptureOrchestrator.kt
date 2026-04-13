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

    suspend fun processImage(bitmap: Bitmap, apiKey: String?): DiagnosticResult {
        val totalLogs = mutableListOf<String>()
        return try {
            if (!apiKey.isNullOrBlank()) {
                Log.d(TAG, "Attempting Tier 1 (Cloud) capture...")
                totalLogs.add("Orchestrator: API Key present, choosing Cloud Engine")
                val result = cloudEngine.processImage(bitmap, apiKey)
                result.copy(logs = totalLogs + result.logs)
            } else {
                Log.d(TAG, "No API key provided. Skipping Tier 1.")
                totalLogs.add("Orchestrator: No API Key, choosing Local Engine")
                val result = localEngine.processImage(bitmap, null)
                result.copy(logs = totalLogs + result.logs)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Tier 1 capture failed. Falling back to Tier 2 (Local).", e)
            totalLogs.add("Orchestrator: Cloud Engine failed (${e.message}), falling back to Local")
            val result = localEngine.processImage(bitmap, null)
            result.copy(logs = totalLogs + result.logs)
        }
    }
}
