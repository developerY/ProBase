package com.zoewave.probase.features.ai.local.data

import android.content.Context
import com.zoewave.probase.features.ai.core.AiInput
import com.zoewave.probase.features.ai.core.AiProvider
import com.zoewave.probase.features.ai.core.AiProviderCapability
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalDownloadAiProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val engineManager: LocalLiteRtLmEngineManager
) : AiProvider {

    override val capability = AiProviderCapability(
        id = "download_ai",
        displayName = "Download AI (Local Execution)",
        maxInputTokens = 1024,
        maxOutputTokens = 1024,
        timeoutMillis = 10000L,
        maxCandidateAdditions = 8,
        minCandidateAdditions = 4,
        isLocal = true,
        supportsLocalImageIngestion = false // LiteRT-LM primarily text in this setup
    )

    override suspend fun isAvailable(): Boolean {
        val modelFile = File(context.filesDir, "model.litertlm")
        return modelFile.exists()
    }

    override suspend fun countTokens(input: AiInput): Int {
        return input.promptString.length / 4
    }

    override suspend fun execute(input: AiInput): Result<String> {
        val modelFile = File(context.filesDir, "model.litertlm")
        if (!modelFile.exists()) {
            return Result.failure(IllegalStateException("Model not downloaded"))
        }

        return try {
            engineManager.initializeEngine(modelFile.absolutePath)

            // LiteRT-LM generation flow
            val responseFlow = engineManager.generateStream(input.promptString)

            // Collect all tokens and combine them
            val fullResponse = responseFlow.toList().joinToString(separator = "")

            Result.success(fullResponse)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
