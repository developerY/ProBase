package com.zoewave.probase.features.ai.capture.data

import android.graphics.Bitmap
import android.util.Log
import com.zoewave.probase.core.model.tasks.SmartTaskDraft
import com.zoewave.probase.features.ai.capture.domain.DiagnosticResult
import com.zoewave.probase.features.ai.capture.domain.SmartCaptureEngine
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class SmartCaptureOrchestrator @Inject constructor(
    @Named("Cloud") private val cloudEngine: SmartCaptureEngine,
    @Named("Local") private val localEngine: SmartCaptureEngine
) {
    private val TAG = "SmartCaptureOrchestrator"

    suspend fun validateApiKey(apiKey: String): Pair<String, List<String>> {
        val models = cloudEngine.getAvailableModels(apiKey)
        return if (models.isNotEmpty()) {
            "Key Valid! Discovered ${models.size} models." to models
        } else {
            "Invalid Key or no models available. Check your API key." to emptyList()
        }
    }

    suspend fun testModel(apiKey: String, modelName: String): String {
        return cloudEngine.testModel(apiKey, modelName)
    }

    suspend fun processImage(
        bitmap: Bitmap?,
        apiKey: String?,
        modelName: String? = null,
        userContext: String? = null,
        onLog: (String) -> Unit = {}
    ): DiagnosticResult<SmartTaskDraft> {
        val totalLogs = mutableListOf<String>()
        
        if (bitmap == null && apiKey.isNullOrBlank()) {
            onLog("No image and no Cloud AI available.")
            totalLogs.add("Orchestrator: Text-only analysis requires Cloud AI")
            return DiagnosticResult(SmartTaskDraft(), totalLogs, error = "Cloud AI is required for text-only analysis.")
        }

        if (!apiKey.isNullOrBlank()) {
            Log.d(TAG, "Attempting Tier 1 (Cloud) capture with $modelName...")
            onLog("Tier 1: Cloud AI requested...")
            totalLogs.add("Orchestrator: Cloud API Key present")
            
            val result = cloudEngine.processImage(bitmap, apiKey, modelName, userContext, onLog)
            
            if (result.error != null) {
                if (bitmap == null) {
                    // No fallback for text-only
                    return result.copy(logs = totalLogs + result.logs)
                }
                Log.w(TAG, "Cloud failed: ${result.error}. Falling back to Local.")
                onLog("Cloud AI failed (${result.error.take(30)}...). Falling back to Local...")
                val fallbackLogs = listOf("Orchestrator: Cloud failed (${result.error})", "Triggering Local AI Fallback")
                val fallbackResult = localEngine.processImage(bitmap, null, userContext = userContext, onLog = onLog)
                return fallbackResult.copy(
                    logs = totalLogs + result.logs + fallbackLogs + fallbackResult.logs,
                    warnings = listOf("Cloud analysis unavailable. Using local extraction.")
                )
            } else {
                return result.copy(logs = totalLogs + result.logs)
            }
        } else {
            Log.d(TAG, "No API key provided. Skipping Tier 1.")
            onLog("Using Tier 2 (Local) AI...")
            totalLogs.add("Orchestrator: No Cloud Key, using Local AI")
            val result = localEngine.processImage(bitmap!!, null, userContext = userContext, onLog = onLog)
            return result.copy(logs = totalLogs + result.logs)
        }
    }
}
