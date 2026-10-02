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
import com.zoewave.probase.kocolor.fashionista.domain.FashionistaEvaluator
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
    private val validator: RecommendationValidator,
    private val fashionistaEvaluator: FashionistaEvaluator,
    private val intentFulfillmentEvaluator: IntentFulfillmentEvaluator
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
                        auditLogger.logAnchorSynthesisResult(requestContext.requestId, blueprint.selectedClothingIds)
                        val isOuterwearForbidden = (requestContext.weatherTempC != null && requestContext.weatherTempC > 17f)
                val thermalStr = when {
                            requestContext.weatherTempC == null -> "MODERATE"
                            requestContext.weatherTempC > 25f -> "HOT"
                            requestContext.weatherTempC > 18f -> "WARM"
                            requestContext.weatherTempC < 10f -> "COLD"
                            else -> "COOL"
                        }
                        
                        auditLogger.logCompositionPolicy(
                            requestId = requestContext.requestId,
                            requiredRoles = if (fitResult.request.promptString.contains("SHOES: exactly 1")) "TOP + BOTTOM + SHOES" else "TOP + BOTTOM",
                            outerwearPermission = if (isOuterwearForbidden) "FORBIDDEN" else "OPTIONAL_IF_ENVIRONMENTALLY_REQUIRED",
                            reason = "Thermal context = $thermalStr"
                        )
                        
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
                        executeAndCache(provider, fitResult, fingerprint, providerStartTime, requestContext) ?: continue
                    }
                }

                val isOuterwearForbidden = (requestContext.weatherTempC != null && requestContext.weatherTempC > 17f)
                val thermalStr = when {
                    requestContext.weatherTempC == null -> "MODERATE"
                    requestContext.weatherTempC > 25f -> "HOT"
                    requestContext.weatherTempC > 18f -> "WARM"
                    requestContext.weatherTempC < 10f -> "COLD"
                    else -> "COOL"
                }
                
                auditLogger.logCompositionPolicy(
                    requestId = requestContext.requestId,
                    requiredRoles = if (fitResult.request.promptString.contains("SHOES: exactly 1")) "TOP + BOTTOM + SHOES" else "TOP + BOTTOM",
                    outerwearPermission = if (isOuterwearForbidden) "FORBIDDEN" else "OPTIONAL_IF_ENVIRONMENTALLY_REQUIRED",
                    reason = "Thermal context = $thermalStr"
                )
                
                val blueprint = executeAndCache(provider, fitResult, fingerprint, providerStartTime, requestContext)
                if (blueprint != null) {
                    auditLogger.logAnchorSynthesisResult(requestContext.requestId, blueprint.selectedClothingIds)
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
        
        val fashionistaScore = fashionistaEvaluator.evaluate(blueprint, requestContext)
        auditLogger.logFashionistaEvaluation(requestContext.requestId, fashionistaScore)

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
        requestContext: StyleRequestContext
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

                if (!validation.isValid) {
                    Log.e("StyleSimulatorEngine", "Validation failed: ${validation.validationErrors.joinToString("; ")}. Halting execution for retry.")
                    logTelemetry(
                        tier = provider.capability.id,
                        kLimit = fitResult.kLimit,
                        detail = fitResult.detailLevel,
                        tokens = fitResult.tokenCount,
                        latency = latency,
                        success = false,
                        reason = "VALIDATION_FAILED"
                    )
                    return null
                }

                val blueprint = validation.sanitizedBlueprint

                val fashionistaScore = fashionistaEvaluator.evaluate(blueprint, requestContext)
                auditLogger.logFashionistaEvaluation(requestContext.requestId, fashionistaScore)

                val selectedClothingItems = fitResult.clothingCandidates.mapNotNull { it.clothingItem }.filter { "w_${it.internalId}" in blueprint.selectedClothingIds || it.remoteId in blueprint.selectedClothingIds }
                val selectedCosmeticItems = fitResult.cosmeticCandidates.mapNotNull { it.cosmeticItem }.filter { "c_${it.internalId}" in blueprint.selectedCosmeticIds || it.remoteId in blueprint.selectedCosmeticIds }

                val intentFulfillment = intentFulfillmentEvaluator.evaluate(
                    intentProfile = requestContext.intentProfile,
                    selectedClothing = selectedClothingItems,
                    selectedCosmetics = selectedCosmeticItems
                )
                auditLogger.logIntentFulfillment(requestContext.requestId, intentFulfillment)

                cache.put(fingerprint, rawResult)
                auditLogger.logAiExecution(
                    requestId = requestContext.requestId,
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
            val cCandidatesProv = candidateFilter.getCosmeticCandidateProvenance(cosmetics, context, limit = currentK)
            val cCandidates = cCandidatesProv.mapNotNull { it.cosmeticItem }
            
            // Critical Guard: Abort if deterministic pruning leaves zero eligible items
            if (selectionState.fullRankedCandidatePool.isEmpty() && selectionState.activeAnchors.isEmpty()) {
                throw IllegalStateException("Your wardrobe has 0 eligible items for this context. Please add more pieces or adjust your environmental filters.")
            }
            
            // Critical Guard: Abort if deterministic pruning leaves zero eligible items
            if (selectionState.fullRankedCandidatePool.isEmpty() && selectionState.activeAnchors.isEmpty()) {
                throw IllegalStateException("Your wardrobe has 0 eligible items for this context. Please add more pieces or adjust your environmental filters.")
            }

            // 1. Convert active anchors to candidate provenance with accurate rationale
            val anchorProv = selectionState.activeAnchors.map { anchor ->
                val isUserLock = context.lockedConstraints.any {
                    (it.itemId == "w_${anchor.internalId}" || it.itemId == anchor.remoteId) &&
                    (it.tier == SelectionTier.LOCKED || it.tier == SelectionTier.FORCED || it.tier == SelectionTier.SELECTED)
                }
                val rationaleText = if (isUserLock) {
                    "[LOCKED ANCHOR] Required outfit anchor"
                } else if (context.intentProfile != null && context.intentProfile.colorfulness > 0.7f) {
                    "[INTENT ANCHOR] High-chroma intent override"
                } else {
                    "Automatic context anchor"
                }
                
                // Adjust contextual score for the anchor itself so it stands out distinctly
                val overrideScore = if (isUserLock || context.intentProfile != null) 4.0f else 2.5f
                CandidateProvenance(
                    clothingItem = anchor,
                    contextScore = overrideScore,
                    colorScore = overrideScore,
                    appearanceScore = overrideScore,
                    freshnessScore = overrideScore,
                    compositeScore = overrideScore,
                    retrievalReason = rationaleText
                )
            }

            // 2. Role Partitioning & Combined Pool
            // The architecture demands slot eligibility first, ranking second.
            val anchorIds = anchorProv.mapNotNull { it.clothingItem?.internalId }.toSet()
            
            // Outerwear permission check
            val isOuterwearForbidden = (context.weatherTempC != null && context.weatherTempC > 17f)
            
            val validCategories = mutableSetOf(ClothingCategory.TOPS, ClothingCategory.BOTTOMS, ClothingCategory.SHOES)
            if (!isOuterwearForbidden) {
                validCategories.add(ClothingCategory.OUTERWEAR)
            }
            
            val eligiblePool = selectionState.fullRankedCandidatePool.filter { 
                (it.clothingItem?.internalId ?: -1) !in anchorIds && it.clothingItem?.category in validCategories
            }
            
            // Group by category to ensure proportional representation rather than letting one category dominate
            val categorizedPool = eligiblePool.groupBy { it.clothingItem?.category }
            
            // We want roughly an even split of the remaining budget (currentK - anchorProv.size)
            val budgetRemaining = (currentK - anchorProv.size).coerceAtLeast(0)
            val slotsPerCategory = if (validCategories.isNotEmpty()) (budgetRemaining / validCategories.size).coerceAtLeast(2) else 3
            
            val partitionedCandidates = mutableListOf<CandidateProvenance>()
            validCategories.forEach { category ->
                val categoryItems = categorizedPool[category]?.take(slotsPerCategory) ?: emptyList()
                partitionedCandidates.addAll(categoryItems)
            }
            
            // Re-sort the combined partitioned pool by score so the highest items are at the top of the manifest
            val sortedPartitionedPool = partitionedCandidates.sortedByDescending { it.compositeScore }
            
            var topWardrobeProv = anchorProv + sortedPartitionedPool

            // 3. Category diversity guarantee (ensure TOPS, BOTTOMS, SHOES are all present)
            val presentCategories = topWardrobeProv.mapNotNull { it.clothingItem?.category }.toSet()
            val missingCategories = mutableListOf<ClothingCategory>()
            if (!presentCategories.contains(ClothingCategory.TOPS)) {
                missingCategories.add(ClothingCategory.TOPS)
            }
            if (!presentCategories.contains(ClothingCategory.BOTTOMS)) {
                missingCategories.add(ClothingCategory.BOTTOMS)
            }
            if (!presentCategories.contains(ClothingCategory.SHOES)) {
                missingCategories.add(ClothingCategory.SHOES)
            }

            if (missingCategories.isNotEmpty()) {
                val currentIds = topWardrobeProv.mapNotNull { it.clothingItem?.internalId }.toSet()
                val supplementaryCandidates = mutableListOf<CandidateProvenance>()
                for (cat in missingCategories) {
                    val suppItem = eligiblePool.find { it.clothingItem?.category == cat && (it.clothingItem?.internalId ?: -1) !in currentIds }
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
                clothingCandidates = topWardrobeProv,
                cosmeticCandidates = cCandidatesProv,
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
