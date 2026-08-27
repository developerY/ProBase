package com.zoewave.probase.features.ai.local.data

import com.zoewave.probase.features.ai.core.AiInput
import com.zoewave.probase.features.ai.core.AiProvider
import com.zoewave.probase.features.ai.core.AiProviderCapability
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalNanoAiProvider @Inject constructor(
    private val engine: LocalAiEngine
) : AiProvider {

    override val capability = AiProviderCapability(
        id = "gemini_nano",
        displayName = "Gemini Nano (On-Device)",
        maxInputTokens = 768,
        maxOutputTokens = 256,
        timeoutMillis = 1200L,
        maxCandidateAdditions = 8,
        minCandidateAdditions = 4,
        isLocal = true,
        supportsLocalImageIngestion = true
    )

    override suspend fun isAvailable(): Boolean {
        return when (engine.checkCapability()) {
            NanoState.Available, NanoState.MultimodalAvailable -> true
            else -> false
        }
    }

    override suspend fun countTokens(input: AiInput): Int {
        return engine.estimateTokens(input.promptString)
    }

    override suspend fun execute(input: AiInput): Result<String> {
        return when (input) {
            is AiInput.Multimodal -> {
                engine.generateMultimodalContent(input.promptString, input.localImage)
            }
            is AiInput.TextOnly -> {
                engine.generateStructuredContent(input.promptString)
            }
        }
    }
}
