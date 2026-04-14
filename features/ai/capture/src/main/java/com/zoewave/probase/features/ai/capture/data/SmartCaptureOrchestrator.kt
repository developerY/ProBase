package com.zoewave.probase.features.ai.capture.data

import android.graphics.Bitmap
import android.util.Log
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
        bitmap: Bitmap,
        apiKey: String?,
        modelName: String? = null,
        userContext: String? = null
    ): DiagnosticResult {
        val totalLogs = mutableListOf<String>()
        return try {
            if (!apiKey.isNullOrBlank()) {
                Log.d(TAG, "Attempting Tier 1 (Cloud) capture with $modelName...")
                totalLogs.add("Orchestrator: Cloud API Key present")
                val result = cloudEngine.processImage(bitmap, apiKey, modelName, userContext)
                result.copy(logs = totalLogs + result.logs)
            } else {
                Log.d(TAG, "No API key provided. Skipping Tier 1.")
                totalLogs.add("Orchestrator: No Cloud Key, using Local AI")
                val result = localEngine.processImage(bitmap, null, userContext = userContext)
                result.copy(logs = totalLogs + result.logs)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Tier 1 capture failed. Falling back to Tier 2 (Local).", e)
            val fallbackLogs = listOf("Orchestrator: Cloud failed (${e.localizedMessage})", "Triggering Local AI Fallback")
            val result = localEngine.processImage(bitmap, null, userContext = userContext)
            result.copy(
                logs = totalLogs + fallbackLogs + result.logs,
                warnings = listOf("Cloud analysis unavailable. Using local extraction.")
            )
        }
    }
}
