package com.zoewave.probase.features.ai.capture.data

import android.graphics.Bitmap
import android.util.Log
import com.zoewave.probase.core.model.tasks.SmartTaskDraft
import com.zoewave.probase.features.ai.capture.domain.DiagnosticResult
import com.zoewave.probase.features.ai.capture.domain.SmartCaptureEngine
import com.zoewave.probase.features.compliance.AgeSignalsManager
import com.zoewave.probase.features.compliance.model.AgeRange
import com.zoewave.probase.features.compliance.model.AgeVerificationStatus
import com.zoewave.probase.features.compliance.model.ComplianceError
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class SmartCaptureOrchestrator @Inject constructor(
    @param:Named("Cloud") private val cloudEngine: SmartCaptureEngine,
    @param:Named("Local") private val localEngine: SmartCaptureEngine,
    private val ageSignalsManager: AgeSignalsManager
) {
    private val tag = "SmartCaptureOrchestrator"

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
            /*
            Log.d(tag, "Performing Compliance Handshake...")
            onLog("Compliance: Verifying age signal...")
            val ageSignalResult = ageSignalsManager.getAgeSignal()
            
            val isAllowed = ageSignalResult.fold(
                onSuccess = { signal ->
                    if (signal.isAuthorizedForCloudAI) {
                        Log.d(tag, "Compliance: Handshake successful (Status: ${signal.verificationStatus}).")
                        onLog("Compliance: Handshake successful.")
                        true
                    } else {
                        Log.w(tag, "Compliance: Access restricted (Status: ${signal.verificationStatus}).")
                        if (signal.verificationStatus == AgeVerificationStatus.SUPERVISED_APPROVAL_PENDING) {
                            onLog("Compliance: Parental approval required.")
                        } else {
                            onLog("Compliance: 13+ Policy check failed.")
                        }
                        false
                    }
                },
                onFailure = { error ->
                    if (error is ComplianceError.SdkVersionOutdated) {
                        Log.e(tag, "Compliance: SDK version outdated. Blocking Cloud AI.")
                        onLog("Compliance: SDK Version Outdated. Please update the app.")
                    } else {
                        Log.e(tag, "Compliance: Error retrieving signal: ${error.message}")
                        onLog("Compliance: Verification error.")
                    }
                    false
                }
            )

            if (!isAllowed) {
                if (bitmap == null) {
                    return DiagnosticResult(SmartTaskDraft(), totalLogs, error = "Compliance: Cloud AI access restricted.")
                }
                Log.w(tag, "Compliance restricted. Falling back to Local.")
                onLog("Compliance restricted. Falling back to Local AI...")
                val fallbackLogs = listOf("Orchestrator: Compliance Check Restricted", "Triggering Local AI Fallback")
                val fallbackResult = localEngine.processImage(bitmap, null, userContext = userContext, onLog = onLog)
                return fallbackResult.copy(
                    logs = totalLogs + fallbackLogs + fallbackResult.logs,
                    warnings = listOf("Cloud analysis restricted for compliance. Using local extraction.")
                )
            }
            */

            Log.d(tag, "Attempting Tier 1 (Cloud) capture with $modelName...")
            onLog("Tier 1: Cloud AI requested...")
            totalLogs.add("Orchestrator: Cloud API Key present")
            
            val result = cloudEngine.processImage(bitmap, apiKey, modelName, userContext, onLog)
            
            if (result.error != null) {
                if (bitmap == null) {
                    // No fallback for text-only
                    return result.copy(logs = totalLogs + result.logs)
                }
                Log.w(tag, "Cloud failed: ${result.error}. Falling back to Local.")
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
                Log.d(tag, "No API key provided. Skipping Tier 1.")
                onLog("Using Tier 2 (Local) AI...")
                totalLogs.add("Orchestrator: No Cloud Key, using Local AI")
                val result = localEngine.processImage(bitmap!!, null, userContext = userContext, onLog = onLog)
                return result.copy(logs = totalLogs + result.logs)
            }
    }
}
