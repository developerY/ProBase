package com.zoewave.probase.features.ai.local.data

import com.zoewave.probase.features.ai.core.AiProvider
import com.zoewave.probase.features.ai.core.AiProviderCapability
import com.zoewave.probase.features.ai.core.StylePromptRequest
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
        initialTopK = 8,
        minTopK = 4,
        isLocal = true,
        supportsLocalImageIngestion = true
    )

    override suspend fun isAvailable(): Boolean {
        return when (engine.checkCapability()) {
            NanoState.Available, NanoState.MultimodalAvailable -> true
            else -> false
        }
    }

    override suspend fun countTokens(request: StylePromptRequest): Int {
        return engine.estimateTokens(request.exactPromptString)
    }

    override suspend fun execute(request: StylePromptRequest): Result<String> {
        return engine.generateStructuredContent(request.exactPromptString)
    }
}
