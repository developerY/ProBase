package com.zoewave.probase.kocolor.data.usecase

import android.graphics.Bitmap
import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.core.model.ritual.CosmeticItem
import com.zoewave.probase.core.model.ritual.MacroCategory
import com.zoewave.probase.features.ai.firebase.FirebaseAiClient
import com.zoewave.probase.features.ai.firebase.models.Appearance
import com.zoewave.probase.features.ai.firebase.models.StyleTelemetry
import com.zoewave.probase.features.ai.local.data.LocalAiEngine
import com.zoewave.probase.features.ai.local.data.PromptCacheRepository
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StyleSimulatorEngine @Inject constructor(
    private val localAi: LocalAiEngine,
    private val firebaseAiClient: FirebaseAiClient,
    private val cache: PromptCacheRepository
) {

    companion object {
        private const val MAX_ROTATION_PENALTY = 0.70
        private const val MAX_CLOUD_INPUT_TOKENS = 3000
        private const val RETRIEVAL_POLICY_VERSION = "1.0"
        private const val PROMPT_VERSION = "1.0"
        private val NOISE_CATEGORIES = setOf("oral", "tools", "fragrance", "grooming", "organizers")
    }

    private val json = Json { 
        ignoreUnknownKeys = true 
        coerceInputValues = true
    }

    private val BLUEPRINT_SCHEMA = """
        {
          "rationale": "string",
          "selectedClothingIds": ["String", "String", "String"],
          "selectedCosmeticIds": ["String", "String", "String", ...],
          "recommendedPalette": ["#HEX", "#HEX", "#HEX", "#HEX"]
        }
    """.trimIndent()

    /**
     * Cascading execution with Type-Safe Boundaries as per Secure Vertex AI Integration plan.
     */
    suspend fun architectStyleBlueprint(
        userIntent: String,
        circadianContext: String,
        routineCompleted: Boolean,
        wellnessScore: Double,
        weatherContext: String,
        availableWardrobe: List<ClothingItem>,
        availableCosmetics: List<CosmeticItem>,
        rotationScores: Map<String, Double> = emptyMap(),
        fashionProfile: String? = null,
        anchoredClothing: List<ClothingItem> = emptyList(),
        anchoredCosmetics: List<CosmeticItem> = emptyList(),
        appearance: Appearance? = null,
        portrait: Bitmap? = null,
        useFirebase: Boolean = true,
        apiKey: String? = null,
        modelName: String = "gemini-3.5-flash-lite"
    ): StyleBlueprint {
        val startTime = System.currentTimeMillis()
        var executionTier = "Tier 2"
        var fallbackReason: String? = null
        var estimatedInputTokens = 0
        var actualPromptTokens = 0
        var completionTokens = 0
        var totalTokens = 0
        var modelUsed = modelName

        // 1. Manifest Minification (Phase 2)
        val minResult = minifyManifest(
            availableWardrobe, availableCosmetics, anchoredClothing, anchoredCosmetics, rotationScores
        )
        val minifiedManifest = minResult.manifest

        // 2. Semantic Caching (Phase 3)
        val fingerprint = cache.generateFingerprint(
            promptVersion = PROMPT_VERSION,
            modelVersion = modelUsed,
            retrievalPolicyVersion = RETRIEVAL_POLICY_VERSION,
            appearanceTelemetry = appearance?.toString() ?: "no_appearance",
            weatherState = weatherContext,
            userIntent = userIntent,
            minifiedManifest = minifiedManifest
        )

        val cachedResponse = cache.get(fingerprint)
        if (cachedResponse != null) {
            executionTier = "Cache"
            logTelemetry(
                cacheHit = true,
                fingerprint = fingerprint,
                minResult = minResult,
                estTokens = 0,
                actPrompt = 0,
                compTokens = 0,
                totTokens = 0,
                tier = executionTier,
                reason = null,
                model = modelUsed
            )
            return sanitizeAndDecode(cachedResponse)
        }

        // 3. Token Budgeting & Telemetry (Phase 4)
        
        // Tier 0: Enterprise Production Route (Firebase AI Logic)
        if (useFirebase && appearance != null) {
            val styleTelemetry = StyleTelemetry(
                appearance = appearance,
                vaultManifest = minifiedManifest,
                weatherContext = weatherContext,
                circadianContext = circadianContext
            )

            try {
                estimatedInputTokens = firebaseAiClient.estimateTokens(styleTelemetry, userIntent)
                if (estimatedInputTokens <= MAX_CLOUD_INPUT_TOKENS) {
                    Log.d("StyleSimulatorEngine", "THINKING: Attempting Tier 0 (Firebase AI Logic)...")
                    val response = firebaseAiClient.getStyleAdvice(styleTelemetry, userIntent)
                    cache.put(fingerprint, response.text)
                    executionTier = "Tier 0"
                    actualPromptTokens = response.promptTokens
                    completionTokens = response.completionTokens
                    totalTokens = response.totalTokens
                    modelUsed = response.modelName
                    
                    logTelemetry(false, fingerprint, minResult, estimatedInputTokens, 
                                 actualPromptTokens, completionTokens, totalTokens, executionTier, null, modelUsed)
                    return sanitizeAndDecode(response.text)
                } else {
                    fallbackReason = "TOKEN_BUDGET"
                    Log.w("StyleSimulatorEngine", "Tier 0 skipped: TOKEN_BUDGET ($estimatedInputTokens > $MAX_CLOUD_INPUT_TOKENS)")
                }
            } catch (e: Exception) {
                fallbackReason = "NETWORK_FAILURE"
                Log.e("StyleSimulatorEngine", "Tier 0 failed, falling back to Tier 1.5...", e)
            }
        }

        // Tier 1.5: Local LLM (Gemini Nano)
        Log.d("StyleSimulatorEngine", "THINKING: Attempting Tier 1.5 (On-Device Gemini Nano)...")
        try {
            val localPrompt = buildArchitectPrompt(
                userIntent, circadianContext, routineCompleted, wellnessScore, weatherContext, 
                minifiedManifest, fashionProfile
            )
            
            // Token Preflight for Local AI
            val localTokens = localAi.estimateTokens(localPrompt)
            
            val result = if (portrait != null) {
                localAi.generateMultimodalContent(localPrompt, portrait, BLUEPRINT_SCHEMA)
            } else {
                localAi.generateStructuredContent(localPrompt, BLUEPRINT_SCHEMA)
            }

            val text = result.getOrNull()
            if (text != null) {
                cache.put(fingerprint, text)
                executionTier = "Tier 1.5"
                modelUsed = "gemini-nano"
                logTelemetry(false, fingerprint, minResult, localTokens, 0, 0, 0, executionTier, fallbackReason, modelUsed)
                Log.d("StyleSimulatorEngine", "SUCCESS: Blueprint generated via Tier 1.5 in ${System.currentTimeMillis() - startTime}ms")
                return sanitizeAndDecode(text)
            } else {
                Log.w("StyleSimulatorEngine", "Tier 1.5 returned null result")
            }
        } catch (e: Exception) {
            Log.w("StyleSimulatorEngine", "Tier 1.5 failed, checking Tier 1 fallback...", e)
        }

        // Tier 1: Probabilistic (Cloud Gemini BYOK) - Deep Fallback
        if (!apiKey.isNullOrBlank()) {
            Log.d("StyleSimulatorEngine", "THINKING: Attempting Tier 1 (Cloud Gemini Fallback)...")
            try {
                val prompt = buildArchitectPrompt(
                    userIntent, circadianContext, routineCompleted, wellnessScore, weatherContext, 
                    minifiedManifest, fashionProfile
                )
                val cloudResponse = architectCloudBlueprint(prompt, apiKey, modelName)
                if (cloudResponse != null) {
                    executionTier = "Tier 1"
                    actualPromptTokens = cloudResponse.promptTokens
                    completionTokens = cloudResponse.completionTokens
                    totalTokens = cloudResponse.totalTokens
                    
                    logTelemetry(false, fingerprint, minResult, 0, actualPromptTokens, completionTokens, totalTokens, executionTier, fallbackReason, modelName)
                    Log.d("StyleSimulatorEngine", "SUCCESS: Blueprint generated via Tier 1 in ${System.currentTimeMillis() - startTime}ms")
                    return sanitizeAndDecode(cloudResponse.text)
                }
            } catch (e: Exception) {
                Log.e("StyleSimulatorEngine", "Tier 1 fallback failed", e)
            }
        }

        // Tier 2: Deterministic (Local Heuristics) - Final Safety Net
        Log.d("StyleSimulatorEngine", "THINKING: Attempting Tier 2 (Local Heuristic Architect)...")
        val localResult = getDeterministicAdvice(userIntent, availableWardrobe, availableCosmetics)
        logTelemetry(false, fingerprint, minResult, 0, 0, 0, 0, "Tier 2", fallbackReason, "heuristic")
        Log.d("StyleSimulatorEngine", "SUCCESS: Blueprint generated via Tier 2 in ${System.currentTimeMillis() - startTime}ms")
        return localResult
    }

    private fun logTelemetry(
        cacheHit: Boolean,
        fingerprint: String,
        minResult: MinifiedResult,
        estTokens: Int,
        actPrompt: Int,
        compTokens: Int,
        totTokens: Int,
        tier: String,
        reason: String?,
        model: String
    ) {
        Log.d("KoColor_Telemetry", """
            - cache_hit: $cacheHit
            - cache_key: ${fingerprint.take(8)}
            - vault_size: ${minResult.vaultSize}
            - eligible_count: ${minResult.eligibleCount}
            - candidates_sent: ${minResult.candidatesSent}
            - estimated_input_tokens: $estTokens
            - actual_prompt_tokens: $actPrompt
            - completion_tokens: $compTokens
            - total_tokens: $totTokens
            - execution_tier: $tier
            - fallback_reason: $reason
            - model: $model
            - prompt_version: $PROMPT_VERSION
            - retrieval_policy_version: $RETRIEVAL_POLICY_VERSION
        """.trimIndent())
    }

    private data class MinifiedResult(
        val manifest: String,
        val vaultSize: Int,
        val eligibleCount: Int,
        val candidatesSent: Int
    )

    private fun minifyManifest(
        wardrobe: List<ClothingItem>,
        cosmetics: List<CosmeticItem>,
        anchoredWardrobe: List<ClothingItem>,
        anchoredCosmetics: List<CosmeticItem>,
        rotationScores: Map<String, Double>
    ): MinifiedResult {
        val vaultSize = wardrobe.size + cosmetics.size

        // Pruning noise categories and low-rotation candidates
        val eligibleWardrobe = wardrobe.filter { item ->
            val penalty = rotationScores[item.remoteId] ?: 0.0
            penalty < MAX_ROTATION_PENALTY
        }

        val eligibleCosmetics = cosmetics.filter { item ->
            val category = item.macroCategory.name.lowercase()
            !NOISE_CATEGORIES.contains(category)
        }
        
        val eligibleCount = eligibleWardrobe.size + eligibleCosmetics.size

        // Further pruning based on anchors
        val anchoredCategories = anchoredWardrobe.map { it.category }.toSet()
        val prunedWardrobe = eligibleWardrobe.filter { item ->
            if (anchoredCategories.contains(item.category)) {
                anchoredWardrobe.any { it.internalId == item.internalId }
            } else true
        }

        val anchoredCosmeticMacros = anchoredCosmetics.map { it.macroCategory }.toSet()
        val prunedCosmetics = eligibleCosmetics.filter { item ->
            if (anchoredCosmeticMacros.contains(item.macroCategory)) {
                anchoredCosmetics.any { it.internalId == item.internalId }
            } else true
        }
        
        val candidatesSent = prunedWardrobe.size + prunedCosmetics.size

        // Mapping to [ID, SemanticType, HexColor]
        val minWardrobe = prunedWardrobe.groupBy { it.category.name.lowercase() }
            .mapValues { (_, items) ->
                items.map { 
                    listOf(
                        "w_${it.internalId}", 
                        it.name.lowercase(), 
                        it.colorHex ?: "#000000"
                    ) 
                }
            }

        val minCosmetics = prunedCosmetics.groupBy { it.macroCategory.name.lowercase() }
            .mapValues { (_, items) ->
                items.map { 
                    listOf(
                        "c_${it.internalId}", 
                        it.microCategory.name.lowercase(), 
                        it.colorHex ?: "#000000"
                    ) 
                }
            }

        val manifest = json.encodeToString(CloudManifest(minWardrobe, minCosmetics))
        return MinifiedResult(manifest, vaultSize, eligibleCount, candidatesSent)
    }

    private fun getDeterministicAdvice(
        userIntent: String,
        availableWardrobe: List<ClothingItem>,
        availableCosmetics: List<CosmeticItem>
    ): StyleBlueprint {
        return architectLocalBlueprint(userIntent, availableWardrobe, availableCosmetics)
    }

    private data class InternalAiResponse(
        val text: String,
        val promptTokens: Int,
        val completionTokens: Int,
        val totalTokens: Int
    )

    private suspend fun architectCloudBlueprint(
        prompt: String,
        apiKey: String,
        modelName: String
    ): InternalAiResponse? {
        // Strip "models/" prefix if present, as the SDK handles it
        val sanitizedModelName = modelName.removePrefix("models/")
        
        val generativeModel = GenerativeModel(
            modelName = sanitizedModelName,
            apiKey = apiKey,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
            }
        )

        val inputContent = content {
            text(prompt)
        }

        // Execute Tier 1 BYOK Request with Logging
        Log.d("KoColorAI_IO", ">>> REQUEST TO GEMINI (BYOK):\n$prompt")

        val response = generativeModel.generateContent(inputContent)
        val jsonText = response.text ?: return null

        Log.d("KoColorAI_IO", "^^^ RESPONSE FROM GEMINI (BYOK):\n$jsonText")

        val metadata = response.usageMetadata
        if (metadata != null) {
            Log.d("KoColorAI_IO", "TOKEN USAGE (BYOK): Prompt=${metadata.promptTokenCount}, Candidates=${metadata.candidatesTokenCount}, Total=${metadata.totalTokenCount}")
        }

        Log.d("StyleSimulatorEngine", "DATA_IN (Cloud Response Raw): $jsonText")
        
        return InternalAiResponse(
            text = jsonText,
            promptTokens = metadata?.promptTokenCount ?: 0,
            completionTokens = metadata?.candidatesTokenCount ?: 0,
            totalTokens = metadata?.totalTokenCount ?: 0
        )
    }

    private fun buildArchitectPrompt(
        userIntent: String,
        circadianContext: String,
        routineCompleted: Boolean,
        wellnessScore: Double,
        weatherContext: String,
        minifiedManifest: String,
        fashionProfile: String?
    ): String {
        return """
            You are the KoColor Style Architect AI. Generate a "Style Blueprint" that is both stylistically harmonic and protective.
            
            USER INTENT: $userIntent
            CIRCADIAN & WELLNESS CONTEXT: $circadianContext (Wellness: ${"%.2f".format(wellnessScore)}, Ritual Done: $routineCompleted)
            WEATHER/ATMOSPHERIC: $weatherContext
            SKIN PROFILE: ${fashionProfile ?: "Unknown"}
            
            AVAILABLE VAULT (MATRIX REPRESENTATION):
            Legend:
            - Clothing Schema: [ID, Name/Type, ColorHex]
            - Cosmetic Schema: [ID, MicroCategory, ColorHex]
            - IDs: Prefixed with 'w_' for Wardrobe and 'c_' for Cosmetics.
            
            $minifiedManifest
            
            GOAL:
            1. Select BEST 3 clothing items (Top, Bottom, Shoes) from the wardrobe section of the manifest. 
            2. Select exactly 4 PIGMENT makeup items (1 Eye, 1 Cheek, 1 Lip, 1 Nail) strictly from the cosmetics section of the manifest. Prioritize user anchors.
            3. Select 1-2 DEFENSIVE items (Complexion/Skincare) from the cosmetics section based strictly on the WEATHER/ATMOSPHERIC data. 
               - If UV is high, select an SPF product.
               - If humidity/heat is high, select a matte/long-wear foundation or primer.
            4. Create a 4-color Palette (HEX codes) harmonizing the whole look. The 4th color MUST be the selected Nail color.
            5. Provide a brief rationale. Mention WHY you selected the specific DEFENSIVE items for the current weather and why you chose the nail color. 
               CRITICAL SYNTAX RULE: When referencing ANY selected item in your rationale, you MUST use the exact inline tag format <ITEM:id>. Do not attempt to guess or describe the item's brand in the text.
               EXAMPLE RATIONALE: "The <ITEM:w_55> is selected because the weather requires <ITEM:c_151>."
            
            Respond ONLY with a valid JSON object matching this schema:
            {
              "rationale": "string",
              "selectedClothingIds": ["w_id", "w_id", "w_id"],
              "selectedCosmeticIds": ["c_id", "c_id", "c_id", "c_id", ...],
              "recommendedPalette": ["#HEX", "#HEX", "#HEX", "#HEX"]
            }
        """.trimIndent()
    }

    private fun sanitizeAndDecode(jsonText: String): StyleBlueprint {
        val cleanedJson = jsonText.substringAfter("{").substringBeforeLast("}")
        val finalJson = "{$cleanedJson}"
        return json.decodeFromString<StyleBlueprint>(finalJson)
    }

    /**
     * Best-effort local stylistic calculation without AI.
     */
    fun architectLocalBlueprint(
        userIntent: String,
        availableWardrobe: List<ClothingItem>,
        availableCosmetics: List<CosmeticItem>
    ): StyleBlueprint {
        val selectedItems = mutableListOf<ClothingItem>()
        val selectedCosmetics = mutableListOf<CosmeticItem>()
        
        // 1. Pick Clothes
        val tops = availableWardrobe.filter { it.category == ClothingCategory.TOPS }
        val bottoms = availableWardrobe.filter { it.category == ClothingCategory.BOTTOMS }
        val shoes = availableWardrobe.filter { it.category == ClothingCategory.SHOES }
        val accessories = availableWardrobe.filter { it.category == ClothingCategory.ACCESSORIES }
        
        fun <T> List<T>.smartPick(nameSelector: (T) -> String, notesSelector: (T) -> String?): T? {
            if (this.isEmpty()) return null
            val matches = this.filter { item ->
                userIntent.split(" ").any { keyword -> 
                    nameSelector(item).contains(keyword, ignoreCase = true) || 
                    (notesSelector(item)?.contains(keyword, ignoreCase = true) ?: false)
                }
            }
            return matches.randomOrNull() ?: this.random()
        }

        tops.smartPick({it.name}, {it.notes})?.let { selectedItems.add(it) }
        bottoms.smartPick({it.name}, {it.notes})?.let { selectedItems.add(it) }
        shoes.smartPick({it.name}, {it.notes})?.let { selectedItems.add(it) }

        // Fallback: If we still don't have enough items, grab accessories
        if (selectedItems.isEmpty() && accessories.isNotEmpty()) {
            accessories.smartPick({it.name}, {it.notes})?.let { selectedItems.add(it) }
        }
        
        // 2. Pick Cosmetics (Trinity: Eyes, Cheeks, Lips, Nails)
        val eyes = availableCosmetics.filter { it.macroCategory == MacroCategory.EYES }
        val cheeks = availableCosmetics.filter { it.macroCategory == MacroCategory.DIMENSION }
        val lips = availableCosmetics.filter { it.macroCategory == MacroCategory.LIPS }
        val nails = availableCosmetics.filter { it.macroCategory == MacroCategory.NAILS }

        eyes.smartPick({it.name}, {it.notes})?.let { selectedCosmetics.add(it) }
        cheeks.smartPick({it.name}, {it.notes})?.let { selectedCosmetics.add(it) }
        lips.smartPick({it.name}, {it.notes})?.let { selectedCosmetics.add(it) }
        nails.smartPick({it.name}, {it.notes})?.let { selectedCosmetics.add(it) }

        val palette = selectedItems.mapNotNull { it.dominantHex }.distinct().toMutableList()
        if (palette.isEmpty()) palette.add("#FFFFFF")
        
        selectedCosmetics.forEach { item ->
            item.colorHex?.let { palette.add(it) }
        }

        while (palette.size < 4) {
            palette.add(listOf("#000000", "#808080", "#E0E0E0", "#333333", "#8B0000").random())
        }
        val finalPalette = palette.take(4)

        val itemNames = selectedItems.joinToString(", ") { it.name }
        val rationale = if (itemNames.isNotBlank()) {
            "Optimized for rotation. Features your $itemNames."
        } else {
            "Local Architect: Selected from your vault based on intent and freshness score."
        }

        return StyleBlueprint(
            rationale = rationale,
            selectedClothingIds = selectedItems.map { "w_${it.internalId}" },
            selectedCosmeticIds = selectedCosmetics.map { "c_${it.internalId}" },
            recommendedPalette = finalPalette
        )
    }
}

@Serializable
data class StyleBlueprint(
    val rationale: String,
    val selectedClothingIds: List<String>,
    val selectedCosmeticIds: List<String>,
    val recommendedPalette: List<String>
)

@Serializable
private data class CloudManifest(
    val wardrobe: Map<String, List<List<String>>>,
    val cosmetics: Map<String, List<List<String>>>
)
