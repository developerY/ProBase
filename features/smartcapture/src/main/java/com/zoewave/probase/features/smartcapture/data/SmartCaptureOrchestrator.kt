package com.zoewave.probase.features.smartcapture.data

import android.graphics.Bitmap
import android.util.Log
import com.zoewave.probase.features.smartcapture.domain.SmartCaptureEngine
import com.zoewave.probase.features.smartcapture.domain.TaskDraftState
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class SmartCaptureOrchestrator @Inject constructor(
    @Named("Cloud") private val cloudEngine: SmartCaptureEngine,
    @Named("Local") private val localEngine: SmartCaptureEngine
) {
    private val TAG = "SmartCaptureOrchestrator"

    suspend fun processImage(bitmap: Bitmap, apiKey: String?): TaskDraftState {
        return try {
            if (!apiKey.isNullOrBlank()) {
                Log.d(TAG, "Attempting Tier 1 (Cloud) capture...")
                cloudEngine.processImage(bitmap, apiKey)
            } else {
                Log.d(TAG, "No API key provided. Skipping Tier 1.")
                localEngine.processImage(bitmap, null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Tier 1 capture failed. Falling back to Tier 2 (Local).", e)
            localEngine.processImage(bitmap, null)
        }
    }
}
