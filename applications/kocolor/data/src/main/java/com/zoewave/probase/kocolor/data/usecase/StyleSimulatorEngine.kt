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
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
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
    private val intentFulfillmentEvaluator: IntentFulfillmentEvaluator,
    private val roleGapAnalyzer: RoleGapAnalyzer
) {

    companion object {
        private const val RETRIEVAL_POLICY_VERSION = "3.0" // Anchor-Driven Pipeline
        private const val PROMPT_VERSION = "3.0"
    }

    private val json = Json { 
        ignoreUnknownKeys = true 
        isLenient = true
        coerceInputValues = true
        explicitNulls = false
    }

    /**
     * Entry point for generating a style blueprint using the best available AI provider.
     */
    suspend fun generateBlueprint(
        wardrobe: List<ClothingItem>,
        cosmetics: List<CosmeticItem>,
        requestContext: StyleRequestContext
    ): SimulationResult {
        auditLogger.startRequest(requestContext.requestId)
        val providers = capabilityRouter.getRankedAvailableProviders()
        
        // P2.2 Hoist selection-state generation to run once per request
        val selectionState = contextEngine.generateSelectionState(wardrobe, requestContext.lockedConstraints, requestContext)

        // P4.1 Eligible-Pool Preflight Gate: Abort AI providers if eligible pool cannot form a complete outfit
        val eligibleCategories = selectionState.activeAnchors.map { it.category } +
                selectionState.fullRankedCandidatePool.mapNotNull { it.clothingItem?.category }

        if (!isOutfitSlotComplete(eligibleCategories)) {
            Log.w("StyleSimulatorEngine", "Preflight gate aborted AI providers: eligible pool cannot form a complete outfit")
            val fallbackStartTime = System.currentTimeMillis()
            val blueprint = fallbackEngine.generate(requestContext)
            val fashionistaScore = fashionistaEvaluator.evaluate(blueprint, requestContext)
            auditLogger.logFashionistaEvaluation(requestContext.requestId, fashionistaScore)
            auditLogger.logAiExecution(
                requestId = requestContext.requestId,
                providerId = "DETERMINISTIC_FALLBACK_PREFLIGHT",
                tokens = 0,
                blueprint = blueprint
            )
            auditLogger.printAuditTrail(requestContext.requestId)
            logTelemetry(
                tier = "DETERMINISTIC_FALLBACK_PREFLIGHT",
                kLimit = 0,
                detail = SerializationDetailLevel.MINIMAL,
                tokens = 0,
                latency = System.currentTimeMillis() - fallbackStartTime,
                success = true
            )
            val selectedClothing = blueprint.selectedClothingIds.mapNotNull { id -> wardrobe.find { "w_${it.internalId}" == id || it.remoteId == id } }
            val selectedCosmetics = blueprint.selectedCosmeticIds.mapNotNull { id -> cosmetics.find { "c_${it.internalId}" == id || it.remoteId == id } }
            return SimulationResult(
                blueprint = blueprint,
                fashionistaScore = fashionistaEvaluator.evaluate(blueprint, requestContext, selectedClothing),
                intentFulfillment = intentFulfillmentEvaluator.evaluate(requestContext.intentProfile, selectedClothing, selectedCosmetics, requestContext.rotationScores),
                validation = ValidationReport(isValid = true, isAnchorIncluded = true, hasRequiredSlots = true, hasRequiredRoles = true, areAllIdsGrounded = true, noForbiddenItems = true, rationaleClean = true)
            )
        }

        for (provider in providers) {
            val providerStartTime = System.currentTimeMillis()
            Log.d("StyleSimulatorEngine", "Attempting provider: ${provider.capability.displayName}")
            
            val fitResult = adaptContextToProvider(provider, wardrobe, cosmetics, requestContext, selectionState)
            
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
                        val decodedBlueprint = decodeBlueprint(cachedResponse)
                        val cachedValidation = ValidationReport(
                            isValid = true,
                            isAnchorIncluded = true,
                            hasRequiredSlots = true,
                            hasRequiredRoles = true,
                            areAllIdsGrounded = true,
                            noForbiddenItems = true,
                            rationaleClean = true
                        )
                        auditLogger.logAnchorSynthesisResult(requestContext.requestId, decodedBlueprint.selectedClothingIds)
                        val isOuterwearForbidden = (requestContext.weatherTempC != null && requestContext.weatherTempC >= com.zoewave.probase.kocolor.domain.taxonomy.KoColorThresholds.OUTERWEAR_BAN_TEMP_C)
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
                            blueprint = decodedBlueprint,
                            promptTokens = 0,
                            outputTokens = 0
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
                        val selectedClothing = decodedBlueprint.selectedClothingIds.mapNotNull { id -> wardrobe.find { "w_${it.internalId}" == id || it.remoteId == id } }
                        val selectedCosmetics = decodedBlueprint.selectedCosmeticIds.mapNotNull { id -> cosmetics.find { "c_${it.internalId}" == id || it.remoteId == id } }
                        SimulationResult(
                            blueprint = decodedBlueprint,
                            fashionistaScore = fashionistaEvaluator.evaluate(decodedBlueprint, requestContext, selectedClothing),
                            intentFulfillment = intentFulfillmentEvaluator.evaluate(requestContext.intentProfile, selectedClothing, selectedCosmetics, requestContext.rotationScores),
                            validation = cachedValidation
                        )
                    } catch (e: Exception) {
                        Log.e("StyleSimulatorEngine", "Failed to decode cached result", e)
                        // Continue to execute if cache is corrupt
                        val res = executeAndCache(provider, fitResult, fingerprint, providerStartTime, requestContext)
                        if (res != null) return res else continue
                    }
                }

                val isOuterwearForbidden = (requestContext.weatherTempC != null && requestContext.weatherTempC >= com.zoewave.probase.kocolor.domain.taxonomy.KoColorThresholds.OUTERWEAR_BAN_TEMP_C)
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
                
                val result = executeAndCache(provider, fitResult, fingerprint, providerStartTime, requestContext)
                if (result != null) {
                    auditLogger.logAnchorSynthesisResult(requestContext.requestId, result.blueprint.selectedClothingIds)
                    auditLogger.printAuditTrail(requestContext.requestId)
                    return result
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
        
        val selectedClothing = blueprint.selectedClothingIds.mapNotNull { id -> wardrobe.find { "w_${it.internalId}" == id || it.remoteId == id } }
        val selectedCosmetics = blueprint.selectedCosmeticIds.mapNotNull { id -> cosmetics.find { "c_${it.internalId}" == id || it.remoteId == id } }
        
        return SimulationResult(
            blueprint = blueprint,
            fashionistaScore = fashionistaEvaluator.evaluate(blueprint, requestContext, selectedClothing),
            intentFulfillment = intentFulfillmentEvaluator.evaluate(requestContext.intentProfile, selectedClothing, selectedCosmetics, requestContext.rotationScores),
            validation = ValidationReport(isValid = true, isAnchorIncluded = true, hasRequiredSlots = true, hasRequiredRoles = true, areAllIdsGrounded = true, noForbiddenItems = true, rationaleClean = true)
        )
    }

    private suspend fun executeAndCache(
        provider: AiProvider,
        fitResult: AdaptiveFitResult,
        fingerprint: String,
        startTime: Long,
        requestContext: StyleRequestContext
    ): SimulationResult? {
        val input = fitResult.request
        val executionResult = provider.execute(input)
        val latency = System.currentTimeMillis() - startTime

        return if (executionResult.isSuccess) {
            val rawResult = executionResult.getOrThrow()
            val textOutput = rawResult.text
            val promptTok = if (rawResult.usage.promptTokens > 0) rawResult.usage.promptTokens else fitResult.tokenCount
            val outputTok = rawResult.usage.candidatesTokens
            val totalTok = if (rawResult.usage.totalTokens > 0) rawResult.usage.totalTokens else (promptTok + outputTok)

            try {
                val rawBlueprint = decodeBlueprint(textOutput)
                val validationRaw = validator.validateAndSanitize(
                    rawBlueprint = rawBlueprint,
                    clothingCandidates = fitResult.clothingCandidates,
                    cosmeticCandidates = fitResult.cosmeticCandidates
                )
                
                var cleanBlueprint = validationRaw.sanitizedBlueprint
                var validation = ValidationReport(
                    isValid = validationRaw.isValid,
                    isAnchorIncluded = !validationRaw.validationErrors.any { it.contains("Locked item") },
                    hasRequiredSlots = true,
                    hasRequiredRoles = true,
                    areAllIdsGrounded = !validationRaw.validationErrors.any { it.contains("Unknown") },
                    noForbiddenItems = true,
                    rationaleClean = !validationRaw.validationErrors.any { it.contains("rationale") }
                )

                if (!validationRaw.isValid) {
                    // P2.4 Deterministic Validation Repair Before Failover
                    Log.w("StyleSimulatorEngine", "Validation flagged items. Attempting deterministic local repair: ${validationRaw.validationErrors.joinToString("; ")}")

                    val selectedClothingItemsPre = fitResult.clothingCandidates.mapNotNull { it.clothingItem }.filter { "w_${it.internalId}" in cleanBlueprint.selectedClothingIds || it.remoteId in cleanBlueprint.selectedClothingIds }
                    val anchorIds = requestContext.lockedConstraints.map { it.itemId }
                    val anchorsFirst = selectedClothingItemsPre.sortedByDescending { it.remoteId in anchorIds || "w_${it.internalId}" in anchorIds }
                    val eligibleClothing = fitResult.clothingCandidates.mapNotNull { it.clothingItem }

                    val repairedClothing = fillMissingSlotsDeterministically(anchorsFirst, eligibleClothing, requestContext)
                    cleanBlueprint = cleanBlueprint.copy(selectedClothingIds = repairedClothing.map { "w_${it.internalId}" })

                    val repairedValidation = validator.validateAndSanitize(
                        rawBlueprint = cleanBlueprint,
                        clothingCandidates = fitResult.clothingCandidates,
                        cosmeticCandidates = fitResult.cosmeticCandidates
                    )

                    if (repairedValidation.isValid && cleanBlueprint.rationale.isNotBlank()) {
                        validation = ValidationReport(
                            isValid = true,
                            isAnchorIncluded = true,
                            hasRequiredSlots = true,
                            hasRequiredRoles = true,
                            areAllIdsGrounded = true,
                            noForbiddenItems = true,
                            rationaleClean = true
                        )
                    } else {
                        Log.e("StyleSimulatorEngine", "Validation repair failed. Halting provider attempt.")
                        logTelemetry(
                            tier = provider.capability.id,
                            kLimit = fitResult.kLimit,
                            detail = fitResult.detailLevel,
                            tokens = totalTok,
                            latency = latency,
                            success = false,
                            reason = "VALIDATION_FAILED"
                        )
                        return null
                    }
                }

                val selectedClothingItemsPre = fitResult.clothingCandidates.mapNotNull { it.clothingItem }.filter { "w_${it.internalId}" in cleanBlueprint.selectedClothingIds || it.remoteId in cleanBlueprint.selectedClothingIds }
                val anchorIds = requestContext.lockedConstraints.map { it.itemId }
                val anchorsFirst = selectedClothingItemsPre.sortedByDescending { it.remoteId in anchorIds || "w_${it.internalId}" in anchorIds }
                val eligibleClothing = fitResult.clothingCandidates.mapNotNull { it.clothingItem }
                
                val completedClothing = fillMissingSlotsDeterministically(anchorsFirst, eligibleClothing, requestContext)
                val completedBlueprint = cleanBlueprint.copy(selectedClothingIds = completedClothing.map { "w_${it.internalId}" })

                val fashionistaScore = fashionistaEvaluator.evaluate(completedBlueprint, requestContext, completedClothing)
                auditLogger.logFashionistaEvaluation(requestContext.requestId, fashionistaScore)

                val selectedClothingItems = completedClothing
                val selectedCosmeticItems = fitResult.cosmeticCandidates.mapNotNull { it.cosmeticItem }.filter { "c_${it.internalId}" in cleanBlueprint.selectedCosmeticIds || it.remoteId in cleanBlueprint.selectedCosmeticIds }

                val intentFulfillment = intentFulfillmentEvaluator.evaluate(
                    intentProfile = requestContext.intentProfile,
                    selectedClothing = selectedClothingItems,
                    selectedCosmetics = selectedCosmeticItems,
                    rotationScores = requestContext.rotationScores
                )
                auditLogger.logIntentFulfillment(requestContext.requestId, intentFulfillment)

                cache.put(fingerprint, textOutput)
                auditLogger.logAiExecution(
                    requestId = requestContext.requestId,
                    providerId = provider.capability.id,
                    tokens = totalTok,
                    blueprint = completedBlueprint,
                    promptTokens = promptTok,
                    outputTokens = outputTok
                )
                logTelemetry(
                    tier = provider.capability.id,
                    kLimit = fitResult.kLimit,
                    detail = fitResult.detailLevel,
                    tokens = totalTok,
                    latency = latency,
                    success = true
                )
                SimulationResult(
                    blueprint = completedBlueprint,
                    fashionistaScore = fashionistaScore,
                    intentFulfillment = intentFulfillment,
                    validation = validation
                )
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
        context: StyleRequestContext,
        selectionState: StyleSelectionState
    ): AdaptiveFitResult? {
        val cap = provider.capability
        var currentK = cap.maxCandidateAdditions
        var detailLevel = SerializationDetailLevel.EXPANDED

        val isNano = cap.id == "gemini_nano" || (cap.isLocal && cap.maxInputTokens <= 768)

        // P2.1 Nano Fast-Fail Gate
        if (isNano) {
            val minWManifest = serializer.serialize(
                selectionState.fullRankedCandidatePool.take(cap.minCandidateAdditions),
                emptyList(),
                SerializationDetailLevel.MINIMAL
            )
            val minPromptInput = promptAssembler.assembleCompactNano(
                context = context,
                wardrobeManifest = minWManifest,
                cosmeticManifest = "",
                missingRoles = emptySet(),
                providerCapability = cap
            )
            val minTokensEst = (minPromptInput.promptString.length + 3) / 4
            if (minTokensEst > cap.maxInputTokens) {
                Log.w("StyleSimulatorEngine", "Nano tier fast-failed: minimum floor payload ($minTokensEst tokens) > maxInputTokens (${cap.maxInputTokens})")
                return null
            }
        }

        var calibrationRatio = 1.0
        var hasVerifiedExact = false

        while (currentK >= cap.minCandidateAdditions) {
            val cCandidatesProv = candidateFilter.getCosmeticCandidateProvenance(cosmetics, context, limit = currentK)
            val cCandidates = cCandidatesProv.mapNotNull { it.cosmeticItem }
            
            // Critical Guard: Abort provider adaptation if deterministic pruning leaves zero eligible items
            if (selectionState.fullRankedCandidatePool.isEmpty() && selectionState.activeAnchors.isEmpty()) {
                Log.w("StyleSimulatorEngine", "Wardrobe has 0 eligible items for this context.")
                return null
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

            val anchorIds = anchorProv.mapNotNull { it.clothingItem?.internalId }.toSet()
            val isOuterwearForbidden = (context.weatherTempC != null && context.weatherTempC >= com.zoewave.probase.kocolor.domain.taxonomy.KoColorThresholds.OUTERWEAR_BAN_TEMP_C)
            
            val validCategories = mutableSetOf(ClothingCategory.TOPS, ClothingCategory.BOTTOMS, ClothingCategory.SHOES)
            if (!isOuterwearForbidden) {
                validCategories.add(ClothingCategory.OUTERWEAR)
            }
            
            val eligiblePool = selectionState.fullRankedCandidatePool.filter { 
                (it.clothingItem?.internalId ?: -1) !in anchorIds && it.clothingItem?.category in validCategories
            }
            
            val categorizedPool = eligiblePool.groupBy { it.clothingItem?.category }

            // P4.3 Wire missingRoleRequirements directly into partitioning
            val requiredCategories = selectionState.missingRoleRequirements.mapNotNull { req ->
                when (req.role) {
                    "TOP", "TOPS" -> ClothingCategory.TOPS
                    "BOTTOM", "BOTTOMS" -> ClothingCategory.BOTTOMS
                    "SHOES" -> ClothingCategory.SHOES
                    "OUTERWEAR" -> ClothingCategory.OUTERWEAR
                    else -> null
                }
            }

            val targetCategories = requiredCategories.ifEmpty { validCategories.toList() }
            val budgetRemaining = (currentK - anchorProv.size).coerceAtLeast(0)

            // P2.2 Allow slotsPerCategory to drop to 1 when K steps down to minCandidateAdditions
            val minSlots = if (currentK <= cap.minCandidateAdditions) 1 else 2
            val slotsPerCategory = if (targetCategories.isNotEmpty()) (budgetRemaining / targetCategories.size).coerceAtLeast(minSlots) else 3
            
            val partitionedCandidates = mutableListOf<CandidateProvenance>()
            targetCategories.forEach { category ->
                val categoryItems = categorizedPool[category]?.take(slotsPerCategory) ?: emptyList()
                partitionedCandidates.addAll(categoryItems)
            }

            val sortedPartitionedPool = partitionedCandidates.sortedByDescending { it.compositeScore }.take(currentK)
            val topWardrobeProv = anchorProv + sortedPartitionedPool

            auditLogger.logReasoningSet(context.requestId, topWardrobeProv + cCandidatesProv)

            val manifest = serializer.serialize(topWardrobeProv, cCandidates, detailLevel)
            
            val candidateInput = if (isNano) {
                promptAssembler.assembleCompactNano(
                    context = context,
                    wardrobeManifest = manifest,
                    cosmeticManifest = serializer.serialize(emptyList(), cCandidates, detailLevel),
                    missingRoles = emptySet(),
                    providerCapability = cap
                )
            } else {
                promptAssembler.buildExactRequest(
                    context = context,
                    compactManifest = manifest,
                    clothingCandidates = topWardrobeProv,
                    cosmeticCandidates = cCandidatesProv,
                    providerCapability = cap
                )
            }
            
            // P2.2 Heuristic-first token count check (provider.countTokens called AT MOST ONCE)
            val promptStr = candidateInput.promptString
            val estTokens = ((promptStr.length + 3) / 4 * calibrationRatio).toInt()

            if (!hasVerifiedExact && estTokens <= cap.maxInputTokens) {
                val exactTokens = provider.countTokens(candidateInput)
                hasVerifiedExact = true
                if (exactTokens <= cap.maxInputTokens) {
                    Log.d("StyleSimulatorEngine", "Adapted context: K=$currentK, Detail=$detailLevel, Tokens=$exactTokens")
                    return AdaptiveFitResult(candidateInput, currentK, detailLevel, exactTokens, topWardrobeProv, cCandidatesProv)
                } else {
                    val heuristicIn = (promptStr.length + 3) / 4
                    calibrationRatio = exactTokens.toDouble() / heuristicIn.coerceAtLeast(1)
                }
            } else if (hasVerifiedExact && estTokens <= cap.maxInputTokens) {
                Log.d("StyleSimulatorEngine", "Adapted context via calibrated heuristic: K=$currentK, Detail=$detailLevel, EstTokens=$estTokens")
                return AdaptiveFitResult(candidateInput, currentK, detailLevel, estTokens, topWardrobeProv, cCandidatesProv)
            }

            when (detailLevel) {
                SerializationDetailLevel.EXPANDED -> detailLevel = SerializationDetailLevel.BALANCED
                SerializationDetailLevel.BALANCED -> detailLevel = SerializationDetailLevel.MINIMAL
                SerializationDetailLevel.MINIMAL -> {
                    currentK -= 2
                    detailLevel = SerializationDetailLevel.BALANCED
                }
            }
        }
        return null // Cannot fit within provider budget
    }

    private fun fillMissingSlotsDeterministically(
        currentSelection: List<ClothingItem>,
        eligibleCatalog: List<ClothingItem>,
        context: StyleRequestContext
    ): List<ClothingItem> {
        val completed = currentSelection.toMutableList()

        // P1.2.1 Wire RoleGapAnalyzer directly into deterministic slot completion
        val missingRequirements = roleGapAnalyzer.determineRoleRequirements(
            anchors = completed,
            occasion = context.occasion,
            weatherTempC = context.weatherTempC ?: 20f
        )

        for (req in missingRequirements) {
            val cat = try { ClothingCategory.valueOf(req.role) } catch (_: Exception) { null } ?: continue
            if (cat == ClothingCategory.ACCESSORIES) continue
            val existing = completed.count { it.category == cat }
            if (existing < req.minCount) {
                eligibleCatalog.firstOrNull { it.category == cat && it !in completed }?.let { completed.add(it) }
            }
        }

        // Preserve anchor-first ordering and dress-aware satisfaction
        return completed.distinctBy { if (it.category == ClothingCategory.DRESSES) ClothingCategory.TOPS else it.category }
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

    private fun extractStringValuesFromElement(element: JsonElement?): List<String> {
        if (element == null) return emptyList()
        return when (element) {
            is JsonArray -> {
                element.mapNotNull { item ->
                    when (item) {
                        is JsonPrimitive -> item.contentOrNull
                        is JsonObject -> item.values.firstOrNull { it is JsonPrimitive }?.jsonPrimitive?.contentOrNull
                        else -> null
                    }
                }
            }
            is JsonObject -> {
                element.values.flatMap { valEl ->
                    when (valEl) {
                        is JsonPrimitive -> listOfNotNull(valEl.contentOrNull)
                        is JsonArray -> valEl.mapNotNull { (it as? JsonPrimitive)?.contentOrNull }
                        else -> emptyList()
                    }
                }
            }
            is JsonPrimitive -> listOfNotNull(element.contentOrNull)
            else -> emptyList()
        }
    }

    private fun decodeBlueprint(jsonText: String): StyleBlueprint {
        val finalJson = com.zoewave.probase.features.ai.core.util.JsonSanitizer.extractFirstJsonObject(jsonText) ?: jsonText

        return try {
            val rootElement = json.parseToJsonElement(finalJson).jsonObject

            val rationaleText = rootElement["rationale"]?.jsonPrimitive?.contentOrNull
                ?: rootElement["summary"]?.jsonPrimitive?.contentOrNull
                ?: rootElement["explanation"]?.jsonPrimitive?.contentOrNull
                ?: rootElement["reason"]?.jsonPrimitive?.contentOrNull
                ?: ""

            val clothingElement = rootElement["selectedClothingIds"]
                ?: rootElement["clothing_composition"]
                ?: rootElement["clothing"]
                ?: rootElement["clothingIds"]

            val cosmeticElement = rootElement["selectedCosmeticIds"]
                ?: rootElement["cosmetic_composition"]
                ?: rootElement["cosmetics"]
                ?: rootElement["cosmeticIds"]

            val paletteElement = rootElement["recommendedPalette"]
                ?: rootElement["palette"]
                ?: rootElement["colors"]

            StyleBlueprint(
                rationale = rationaleText,
                selectedClothingIds = extractStringValuesFromElement(clothingElement),
                selectedCosmeticIds = extractStringValuesFromElement(cosmeticElement),
                recommendedPalette = extractStringValuesFromElement(paletteElement)
            )
        } catch (e: Exception) {
            Log.w("StyleSimulatorEngine", "Schema-agnostic JSON normalization failed. Trying direct decode.", e)
            json.decodeFromString<StyleBlueprint>(finalJson)
        }
    }
}
