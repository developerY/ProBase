package com.zoewave.probase.features.smartcapture.data

import android.graphics.Bitmap
import com.zoewave.probase.features.smartcapture.domain.SmartTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmartCaptureCoordinator @Inject constructor(
    private val ocrEngine: MlKitOcrEngine,
    private val aiParser: GeminiNanoParser,
    private val fallbackParser: RegexTaskParser
) {

    suspend fun processImage(bitmap: Bitmap): SmartTask = withContext(Dispatchers.Default) {
        val rawText = ocrEngine.extractText(bitmap)
        if (rawText.isBlank()) return@withContext SmartTask()

        // Try AI first, then fallback
        aiParser.parse(rawText) ?: fallbackParser.parse(rawText)
    }
}
