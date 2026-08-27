package com.zoewave.probase.features.ai.core

/**
 * Capability description for an AI Provider.
 */
data class AiProviderCapability(
    val id: String,
    val displayName: String,
    val maxInputTokens: Int,
    val maxOutputTokens: Int,
    val timeoutMillis: Long,
    val maxCandidateAdditions: Int = 12,
    val minCandidateAdditions: Int = 4,
    val isLocal: Boolean,
    val supportsLocalImageIngestion: Boolean = false
)

/**
 * Unified failure model for AI execution.
 */
sealed interface AiExecutionFailure {
    data object Unavailable : AiExecutionFailure
    data class ContextTooLarge(val details: String) : AiExecutionFailure
    data class QuotaExceeded(val retryAfterMillis: Long? = null) : AiExecutionFailure
    data class Timeout(val elapsedMillis: Long) : AiExecutionFailure
    data class NetworkError(val reason: String) : AiExecutionFailure
    data class ExecutionError(val throwable: Throwable) : AiExecutionFailure
}

/**
 * Common interface for all AI Providers (Local Nano, Firebase AI, BYOK Cloud, etc.)
 */
interface AiProvider {
    val capability: AiProviderCapability
    suspend fun isAvailable(): Boolean
    suspend fun countTokens(input: AiInput): Int
    suspend fun execute(input: AiInput): Result<String>
}
