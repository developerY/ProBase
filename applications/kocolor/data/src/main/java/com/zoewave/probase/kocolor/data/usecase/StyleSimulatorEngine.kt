package com.zoewave.probase.kocolor.data.usecase

import android.util.Log
import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.core.model.ritual.CosmeticItem
import com.zoewave.probase.features.ai.core.AiInput
import com.zoewave.probase.features.ai.core.AiProvider
import com.zoewave.probase.features.ai.local.data.PromptCacheRepository
import com.zoewave.probase.kocolor.data.color.CandidateProvenance
import com.zoewave.probase.kocolor.data.telemetry.StyleAuditLogger
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StyleSimulatorEngine @Inject constructor(
    private val contextEngine: DeterministicContextEngine,
    private val candidateFilter: WardrobeCandidateFilter,
    private val serializer: CompactManifestSerializer,
    private val promptAssembler: PromptAssembler,
    private val capabilityRouter: CapabilityRouter,
    private val cache: PromptCacheRepository,
    private val auditLogger: StyleAuditLogger,
    private val fallbackEngine: DeterministicStyleEngine,
    private val validator: RecommendationValidator
) {

    companion object {
        private const val RETRIEVAL_POLICY_VERSION = "3.0" // Anchor-Driven Pipeline
        private const val PROMPT_VERSION = "3.0"
    }

    private val json = Json { 
        ignoreUnknownKeys = true 
        coerceInputValues = true
    }

    /**
     * Entry point for generating a style blueprint using the best available AI provider.
     */
    suspend fun generateBlueprint(
        wardrobe: List<ClothingItem>,
        cosmetics: List<CosmeticItem>,
        requestContext: StyleRequestContext
    ): StyleBlueprint {
        auditLogger.startRequest(requestContext.requestId)
        val providers = capabilityRouter.getRankedAvailableProviders()
        
        for (provider in providers) {
            val providerStartTime = System.currentTimeMillis()
            Log.d("StyleSimulatorEngine", "Attempting provider: ${provider.capability.displayName}")
            
            val fitResult = adaptContextToProvider(provider, wardrobe, cosmetics, requestContext)
            
            if (fitResult != null) {
                // Phase 3: Deterministic Cache Check
                val fingerprint = cache.generateFingerprint(
                    executionTier = provider.capability.id,
                    promptVersion = PROMPT_VERSION,
                    modelVersion = provider.capability.id,
                    retrievalPolicyVersion = RETRIEVAL_POLICY_VERSION,
                    appearanceTelemetry = requestContext.appearanceProfile.toString(),
                    weatherState = requestContext.weather,
                    userIntent = requestContext.intent,
                    minifiedManifest = fitResult.request.promptString
                )

                val cachedResponse = cache.get(fingerprint)
                if (cachedResponse != null) {
                    return try {
                        val blueprint = decodeBlueprint(cachedResponse)
                        auditLogger.logAiExecution(
                            requestId = requestContext.requestId,
                            providerId = "CACHE_${provider.capability.id}",
                            tokens = 0,
                            blueprint = blueprint
                        )
                        auditLogger.printAuditTrail(requestContext.requestId)
                        logTelemetry(
                            tier = "CACHE_${provider.capability.id}",
                            kLimit = fitResult.kLimit,
                            detail = fitResult.detailLevel,
                            tokens = 0,
                            latency = System.currentTimeMillis() - providerStartTime,
                            success = true
                        )
                        blueprint
                    } catch (e: Exception) {
                        Log.e("StyleSimulatorEngine", "Failed to decode cached result", e)
                        // Continue to execute if cache is corrupt
                        executeAndCache(provider, fitResult, fingerprint, providerStartTime, requestContext.requestId) ?: continue
                    }
                }

                val blueprint = executeAndCache(provider, fitResult, fingerprint, providerStartTime, requestContext.requestId)
                if (blueprint != null) {
                    auditLogger.printAuditTrail(requestContext.requestId)
                    return blueprint
                }
            } else {
                Log.w("StyleSimulatorEngine", "Could not fit context into provider ${provider.capability.id} budget")
                logTelemetry(
                    tier = provider.capability.id,
                    kLimit = provider.capability.maxCandidateAdditions,
                    detail = SerializationDetailLevel.EXPANDED,
                    tokens = 0,
                    latency = System.currentTimeMillis() - providerStartTime,
                    success = false,
                    reason = "CONTEXT_OVERFLOW"
                )
            }
        }
        
        // Indestructible baseline
        val fallbackStartTime = System.currentTimeMillis()
        Log.i("StyleSimulatorEngine", "All providers failed. Falling back to deterministic engine.")
        val blueprint = fallbackEngine.generate(requestContext)
        
        auditLogger.logAiExecution(
            requestId = requestContext.requestId,
            providerId = "DETERMINISTIC_FALLBACK",
            tokens = 0,
            blueprint = blueprint
        )
        auditLogger.printAuditTrail(requestContext.requestId)
        
        logTelemetry(
            tier = "DETERMINISTIC_FALLBACK",
            kLimit = 0,
            detail = SerializationDetailLevel.MINIMAL,
            tokens = 0,
            latency = System.currentTimeMillis() - fallbackStartTime,
            success = true
        )
        
        return blueprint
    }

    private suspend fun executeAndCache(
        provider: AiProvider,
        fitResult: AdaptiveFitResult,
        fingerprint: String,
        startTime: Long,
        requestId: String
    ): StyleBlueprint? {
        val input = fitResult.request
        val executionResult = provider.execute(input)
        val latency = System.currentTimeMillis() - startTime

        return if (executionResult.isSuccess) {
            val rawResult = executionResult.getOrThrow()
            try {
                val rawBlueprint = decodeBlueprint(rawResult)
                val validation = validator.validateAndSanitize(
                    rawBlueprint = rawBlueprint,
                    clothingCandidates = fitResult.clothingCandidates,
                    cosmeticCandidates = fitResult.cosmeticCandidates
                )
                val blueprint = validation.sanitizedBlueprint

                if (validation.validationErrors.isNotEmpty()) {
                    Log.w("StyleSimulatorEngine", "Sanitized blueprint errors: ${validation.validationErrors.joinToString("; ")}")
                }

                cache.put(fingerprint, rawResult)
                auditLogger.logAiExecution(
                    requestId = requestId,
                    providerId = provider.capability.id,
                    tokens = fitResult.tokenCount,
                    blueprint = blueprint
                )
                logTelemetry(
                    tier = provider.capability.id,
                    kLimit = fitResult.kLimit,
                    detail = fitResult.detailLevel,
                    tokens = fitResult.tokenCount,
                    latency = latency,
                    success = true
                )
                blueprint
            } catch (e: Exception) {
                Log.e("StyleSimulatorEngine", "Failed to decode result from ${provider.capability.id}", e)
                logTelemetry(
                    tier = provider.capability.id,
                    kLimit = fitResult.kLimit,
                    detail = fitResult.detailLevel,
                    tokens = fitResult.tokenCount,
                    latency = latency,
                    success = false,
                    reason = "DECODE_ERROR"
                )
                null
            }
        } else {
            val failure = executionResult.exceptionOrNull()
            val reason = failure?.let { it::class.simpleName } ?: "UNKNOWN_FAILURE"
            Log.w("StyleSimulatorEngine", "Provider ${provider.capability.id} failed: ${failure?.message}")
            
            logTelemetry(
                tier = provider.capability.id,
                kLimit = fitResult.kLimit,
                detail = fitResult.detailLevel,
                tokens = fitResult.tokenCount,
                latency = latency,
                success = false,
                reason = reason
            )
            null
        }
    }

    private data class AdaptiveFitResult(
        val request: AiInput,
        val kLimit: Int,
        val detailLevel: SerializationDetailLevel,
        val tokenCount: Int,
        val clothingCandidates: List<CandidateProvenance>,
        val cosmeticCandidates: List<CandidateProvenance>
    )

    /**
     * Step-down strategy to fit the request into a provider's token budget.
     */
    private suspend fun adaptContextToProvider(
        provider: AiProvider,
        wardrobe: List<ClothingItem>,
        cosmetics: List<CosmeticItem>,
        context: StyleRequestContext
    ): AdaptiveFitResult? {
        val cap = provider.capability
        var currentK = cap.maxCandidateAdditions
        var detailLevel = SerializationDetailLevel.EXPANDED

        while (currentK >= cap.minCandidateAdditions) {
            val selectionState = contextEngine.generateSelectionState(wardrobe, context.lockedConstraints, context)
            val cCandidates = candidateFilter.getCosmeticCandidates(cosmetics, context, limit = currentK)
            
            // Map cosmetics to provenance for audit logging
            val cCandidatesProv = cCandidates.map { item ->
                CandidateProvenance(
                    cosmeticItem = item,
                    contextScore = 0.5f,
                    colorScore = 0.8f,
                    appearanceScore = 0.8f,
                    freshnessScore = 1.0f,
                    retrievalReason = if (item.isSignature) "[Signature Item] Rotation bypassed." else "Role diversity match"
                )
            }

            // 1. Convert locked active anchors to candidate provenance (resolves ghost anchor bug)
            val anchorProv = selectionState.activeAnchors.map { anchor ->
                CandidateProvenance(
                    clothingItem = anchor,
                    contextScore = 1.0f,
                    colorScore = 1.0f,
                    appearanceScore = 1.0f,
                    freshnessScore = 1.0f,
                    retrievalReason = "[LOCKED ANCHOR] Required outfit anchor"
                )
            }

            // 2. Combine locked anchors with ranked pool
            val anchorIds = anchorProv.mapNotNull { it.clothingItem?.internalId }.toSet()
            val poolWithoutAnchors = selectionState.fullRankedCandidatePool.filter { (it.clothingItem?.internalId ?: -1) !in anchorIds }
            var topWardrobeProv = (anchorProv + poolWithoutAnchors).take(currentK.coerceAtLeast(anchorProv.size))

            // 3. Category diversity guarantee (ensure TOPS, BOTTOMS, SHOES are all present)
            val presentCategories = topWardrobeProv.mapNotNull { it.clothingItem?.category }.toSet()
            val missingCategories = mutableListOf<ClothingCategory>()
            if (!presentCategories.contains(ClothingCategory.TOPS) && !presentCategories.contains(ClothingCategory.DRESSES)) {
                missingCategories.add(ClothingCategory.TOPS)
            }
            if (!presentCategories.contains(ClothingCategory.BOTTOMS) && !presentCategories.contains(ClothingCategory.DRESSES)) {
                missingCategories.add(ClothingCategory.BOTTOMS)
            }
            if (!presentCategories.contains(ClothingCategory.SHOES)) {
                missingCategories.add(ClothingCategory.SHOES)
            }

            if (missingCategories.isNotEmpty()) {
                val currentIds = topWardrobeProv.mapNotNull { it.clothingItem?.internalId }.toSet()
                val supplementaryCandidates = mutableListOf<CandidateProvenance>()
                for (cat in missingCategories) {
                    val suppItem = poolWithoutAnchors.find { it.clothingItem?.category == cat && (it.clothingItem?.internalId ?: -1) !in currentIds }
                        ?: wardrobe.find { it.category == cat && it.internalId !in currentIds }?.let { item ->
                            CandidateProvenance(
                                clothingItem = item,
                                contextScore = 0.8f,
                                colorScore = 0.8f,
                                appearanceScore = 0.8f,
                                freshnessScore = 1.0f,
                                retrievalReason = "Category balance guarantee (${cat.name})"
                            )
                        }
                    if (suppItem != null) {
                        supplementaryCandidates.add(suppItem)
                    }
                }
                topWardrobeProv = topWardrobeProv + supplementaryCandidates
            }

            auditLogger.logReasoningSet(context.requestId, topWardrobeProv + cCandidatesProv)

            val manifest = serializer.serialize(topWardrobeProv, cCandidates, detailLevel)
            
            val candidateInput = promptAssembler.buildExactRequest(
                context = context,
                compactManifest = manifest,
                providerCapability = cap
            )
            
            val tokenCount = provider.countTokens(candidateInput)
            if (tokenCount <= cap.maxInputTokens) {
                Log.d("StyleSimulatorEngine", "Adapted context: K=$currentK, Detail=$detailLevel, Tokens=$tokenCount")
                return AdaptiveFitResult(candidateInput, currentK, detailLevel, tokenCount, topWardrobeProv, cCandidatesProv)
            }

            when (detailLevel) {
                SerializationDetailLevel.EXPANDED -> {
                    detailLevel = SerializationDetailLevel.BALANCED
                }
                SerializationDetailLevel.BALANCED -> {
                    detailLevel = SerializationDetailLevel.MINIMAL
                }
                SerializationDetailLevel.MINIMAL -> {
                    currentK -= 2
                    detailLevel = SerializationDetailLevel.BALANCED
                }
            }
        }
        return null // Cannot fit within provider budget
    }

    private fun logTelemetry(
        tier: String,
        kLimit: Int,
        detail: SerializationDetailLevel,
        tokens: Int,
        latency: Long,
        success: Boolean,
        reason: String? = null
    ) {
        Log.d("KoColor_Telemetry", """
            - execution_tier_used: $tier
            - retrieval_k_limit: $kLimit
            - serialization_strategy: ${detail.name}
            - tokens_used: $tokens
            - latency_ms: $latency
            - success: $success
            - fallback_reason: $reason
        """.trimIndent())
    }

    private fun decodeBlueprint(jsonText: String): StyleBlueprint {
        val cleanedJson = jsonText.substringAfter("{").substringBeforeLast("}")
        val finalJson = "{$cleanedJson}"
        return json.decodeFromString<StyleBlueprint>(finalJson)
    }
}
