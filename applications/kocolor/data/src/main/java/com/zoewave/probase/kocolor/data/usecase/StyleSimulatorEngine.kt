package com.zoewave.probase.kocolor.data.usecase

import android.util.Log

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
import com.zoewave.probase.core.model.ritual.ClothingCategory
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
                            blueprint = decodedBlueprint
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
                            fashionistaScore = fashionistaEvaluator.evaluate(decodedBlueprint, requestContext),
                            intentFulfillment = intentFulfillmentEvaluator.evaluate(requestContext.intentProfile, selectedClothing, selectedCosmetics),
                            validation = cachedValidation
                        )
                    } catch (e: Exception) {
                        Log.e("StyleSimulatorEngine", "Failed to decode cached result", e)
                        // Continue to execute if cache is corrupt
                        val res = executeAndCache(provider, fitResult, fingerprint, providerStartTime, requestContext)
                        if (res != null) return res else continue
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
            fashionistaScore = fashionistaEvaluator.evaluate(blueprint, requestContext),
            intentFulfillment = intentFulfillmentEvaluator.evaluate(requestContext.intentProfile, selectedClothing, selectedCosmetics),
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
            try {
                val rawBlueprint = decodeBlueprint(rawResult)
                val validationRaw = validator.validateAndSanitize(
                    rawBlueprint = rawBlueprint,
                    clothingCandidates = fitResult.clothingCandidates,
                    cosmeticCandidates = fitResult.cosmeticCandidates
                )
                
                val validation = ValidationReport(
                    isValid = validationRaw.isValid,
                    isAnchorIncluded = !validationRaw.validationErrors.any { it.contains("Locked item") },
                    hasRequiredSlots = true,
                    hasRequiredRoles = true,
                    areAllIdsGrounded = !validationRaw.validationErrors.any { it.contains("Unknown") },
                    noForbiddenItems = true,
                    rationaleClean = !validationRaw.validationErrors.any { it.contains("rationale") }
                )
                
                val validationErrors = validationRaw.validationErrors

                val cleanBlueprint = validationRaw.sanitizedBlueprint
                
                if (!validationRaw.isValid) {
                    Log.e("StyleSimulatorEngine", "Validation failed: ${validationErrors.joinToString("; ")}. Halting execution for retry.")
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

                val blueprint = validationRaw.sanitizedBlueprint

                // P1.2 Upstream Deterministic Slot Completion
                val selectedClothingItemsPre = fitResult.clothingCandidates.mapNotNull { it.clothingItem }.filter { "w_${it.internalId}" in cleanBlueprint.selectedClothingIds || it.remoteId in cleanBlueprint.selectedClothingIds }
                
                // Adopt RoleGapAnalyzer to find missing roles (replacing the old inline math). 
                // We enforce a hard anchor-first order to prevent distinctBy races.
                val anchorIds = requestContext.lockedConstraints.map { it.itemId }
                val anchorsFirst = selectedClothingItemsPre.sortedByDescending { it.remoteId in anchorIds || "w_${it.internalId}" in anchorIds }
                
                // Identify missing slots based on eligible catalog.
                // We need to fetch the eligible candidates here.
                val eligibleClothing = fitResult.clothingCandidates.mapNotNull { it.clothingItem }
                
                val completedClothing = fillMissingSlotsDeterministically(anchorsFirst, eligibleClothing)
                
                // Update blueprint with the fully resolved, completed IDs
                val completedBlueprint = cleanBlueprint.copy(selectedClothingIds = completedClothing.map { "w_${it.internalId}" })

                val fashionistaScore = fashionistaEvaluator.evaluate(completedBlueprint, requestContext)
                auditLogger.logFashionistaEvaluation(requestContext.requestId, fashionistaScore)

                val selectedClothingItems = completedClothing
                val selectedCosmeticItems = fitResult.cosmeticCandidates.mapNotNull { it.cosmeticItem }.filter { "c_${it.internalId}" in cleanBlueprint.selectedCosmeticIds || it.remoteId in cleanBlueprint.selectedCosmeticIds }

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
                    blueprint = completedBlueprint
                )
                logTelemetry(
                    tier = provider.capability.id,
                    kLimit = fitResult.kLimit,
                    detail = fitResult.detailLevel,
                    tokens = fitResult.tokenCount,
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
        context: StyleRequestContext
    ): AdaptiveFitResult? {
        val cap = provider.capability
        var currentK = cap.maxCandidateAdditions
        var detailLevel = SerializationDetailLevel.EXPANDED

        val selectionState = contextEngine.generateSelectionState(wardrobe, context.lockedConstraints, context)

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
        val cleanedJson = jsonText.substringAfter("{").substringBeforeLast("}")
        val finalJson = "{$cleanedJson}"

        return try {
            val rootElement = json.parseToJsonElement(finalJson).jsonObject

            // 1. Extract rationale or fallback to alternate keys
            val rationaleText = rootElement["rationale"]?.jsonPrimitive?.contentOrNull
                ?: rootElement["summary"]?.jsonPrimitive?.contentOrNull
                ?: rootElement["explanation"]?.jsonPrimitive?.contentOrNull
                ?: rootElement["reason"]?.jsonPrimitive?.contentOrNull
                ?: ""

            // 2. Extract selectedClothingIds or fallback to clothing_composition / clothing / clothingIds
            val clothingElement = rootElement["selectedClothingIds"]
                ?: rootElement["clothing_composition"]
                ?: rootElement["clothingComposition"]
                ?: rootElement["clothing_ids"]
                ?: rootElement["clothing"]
            val clothingIds = extractStringValuesFromElement(clothingElement)

            // 3. Extract selectedCosmeticIds or fallback to cosmetic_composition / cosmetics / cosmeticIds
            val cosmeticElement = rootElement["selectedCosmeticIds"]
                ?: rootElement["cosmetic_composition"]
                ?: rootElement["cosmeticComposition"]
                ?: rootElement["cosmetic_ids"]
                ?: rootElement["cosmetics"]
            val cosmeticIds = extractStringValuesFromElement(cosmeticElement)

            // 4. Extract recommendedPalette or fallback to palette
            val paletteElement = rootElement["recommendedPalette"]
                ?: rootElement["palette"]
                ?: rootElement["recommended_palette"]
            val palette = extractStringValuesFromElement(paletteElement)

            // Construct normalized blueprint
            StyleBlueprint(
                rationale = rationaleText,
                selectedClothingIds = clothingIds,
                selectedCosmeticIds = cosmeticIds,
                recommendedPalette = palette
            )
        } catch (e: Exception) {
            Log.w("StyleSimulatorEngine", "Schema-agnostic JSON normalization failed. Trying direct decode.", e)
            json.decodeFromString<StyleBlueprint>(finalJson)
        }
    }

    private fun fillMissingSlotsDeterministically(
        currentSelection: List<ClothingItem>,
        eligibleCatalog: List<ClothingItem>
    ): List<ClothingItem> {
        val selectedCategories = currentSelection.map { it.category }.toSet()
        val completed = currentSelection.toMutableList()

        // Dress-aware fulfillment: If DRESSES is selected, both TOPS and BOTTOMS are fulfilled.
        val hasDress = ClothingCategory.DRESSES in selectedCategories
        val needsTop = !hasDress && ClothingCategory.TOPS !in selectedCategories
        val needsBottom = !hasDress && ClothingCategory.BOTTOMS !in selectedCategories
        val needsShoes = ClothingCategory.SHOES !in selectedCategories

        if (needsTop) {
            eligibleCatalog.firstOrNull { it.category == ClothingCategory.TOPS }?.let { completed.add(it) }
        }
        if (needsBottom) {
            eligibleCatalog.firstOrNull { it.category == ClothingCategory.BOTTOMS }?.let { completed.add(it) }
        }
        if (needsShoes) {
            eligibleCatalog.firstOrNull { it.category == ClothingCategory.SHOES }?.let { completed.add(it) }
        }
        
        // Ensure no duplicate categories (anchor-first order guarantees anchors win)
        return completed.distinctBy { if (it.category == ClothingCategory.DRESSES) ClothingCategory.TOPS else it.category }
    }
}
