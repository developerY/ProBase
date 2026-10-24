package com.zoewave.probase.seaweed.model.finance.ai

import com.zoewave.probase.seaweed.model.finance.validation.DocumentInterpretation
import com.zoewave.probase.seaweed.model.finance.validation.LlmAnalysisBundle

sealed class RouterInterpretationOutcome {
    data class ValidatedSuccess(
        val interpretation: DocumentInterpretation,
        val providerUsed: String,
        val modelIdUsed: String,
        val totalLatencyMs: Long
    ) : RouterInterpretationOutcome()

    data class DeterministicFallbackOnly(
        val partialInterpretation: DocumentInterpretation,
        val fallbackReason: String
    ) : RouterInterpretationOutcome()
}

interface AiInterpretationRouter {
    /**
     * Submits redacted, byte-bounded [LlmAnalysisBundle] chunks to the active AI provider,
     * validates each chunk response via [PostLlmSemanticValidator], and merges chunk results
     * via [ChunkInterpretationMerger]. Must rethrow [kotlinx.coroutines.CancellationException].
     */
    suspend fun interpretDocumentChunks(
        chunks: List<LlmAnalysisBundle>,
        validOcrIds: Set<String>,
        fallbackPartialInterpretation: DocumentInterpretation
    ): RouterInterpretationOutcome
}
