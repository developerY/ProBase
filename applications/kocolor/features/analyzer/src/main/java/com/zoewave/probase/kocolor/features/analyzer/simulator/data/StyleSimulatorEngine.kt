package com.zoewave.probase.kocolor.features.analyzer.simulator.data

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.zoewave.probase.core.model.ritual.*
import com.zoewave.probase.features.ai.local.data.LocalAiEngine
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StyleSimulatorEngine @Inject constructor(
    private val localAi: LocalAiEngine
) {

    private val json = Json { 
        ignoreUnknownKeys = true 
        coerceInputValues = true
    }

    private val BLUEPRINT_SCHEMA = """
        {
          "rationale": "string",
          "selectedClothingIds": ["Long", "Long", "Long"],
          "selectedCosmeticIds": ["Long", "Long", "Long", ...],
          "recommendedPalette": ["#HEX", "#HEX", "#HEX"]
        }
    """.trimIndent()

    suspend fun architectStyleBlueprint(
        userIntent: String,
        circadianContext: String,
        routineCompleted: Boolean,
        wellnessScore: Double,
        weatherContext: String,
        availableWardrobe: List<ClothingItem>,
        availableCosmetics: List<CosmeticItem>,
        fashionProfile: String? = null,
        userPortrait: android.graphics.Bitmap? = null,
        anchoredClothing: List<ClothingItem> = emptyList(),
        anchoredCosmetics: List<CosmeticItem> = emptyList(),
        apiKey: String? = null,
        modelName: String = "gemini-1.5-flash"
    ): StyleBlueprint {
        val startTime = System.currentTimeMillis()
        
        // 1. Manifest Minification (Cloud-Optimization)
        val minifiedManifest = minifyManifest(
            availableWardrobe, availableCosmetics, anchoredClothing, anchoredCosmetics
        )

        val prompt = buildArchitectPrompt(
            userIntent, circadianContext, routineCompleted, wellnessScore, weatherContext, 
            minifiedManifest, fashionProfile
        )

        // Tier 1.5: Local LLM (Gemini Nano) - PREFERRED Tier for speed/cost
        android.util.Log.d("StyleSimulatorEngine", "THINKING: Attempting Tier 1.5 (On-Device Gemini Nano)...")
        try {
            val localAiResult = localAi.generateStructuredContent(prompt, BLUEPRINT_SCHEMA)
            if (localAiResult.isSuccess) {
                val jsonText = localAiResult.getOrThrow()
                android.util.Log.d("StyleSimulatorEngine", "SUCCESS: Blueprint generated via Tier 1.5 in ${System.currentTimeMillis() - startTime}ms")
                return sanitizeAndDecode(jsonText)
            }
        } catch (e: Exception) {
            android.util.Log.w("StyleSimulatorEngine", "THINKING: Tier 1.5 failed (${e.message}), checking Tier 1 fallback...")
        }

        // Tier 1: Probabilistic (Cloud Gemini BYOK Multimodal) - FALLBACK for Nano failures
        if (!apiKey.isNullOrBlank()) {
            android.util.Log.d("StyleSimulatorEngine", "THINKING: Attempting Tier 1 (Cloud Gemini $modelName Fallback)...")
            try {
                val cloudResult = architectCloudBlueprint(prompt, userPortrait, apiKey, modelName)
                if (cloudResult != null) {
                    android.util.Log.d("StyleSimulatorEngine", "SUCCESS: Blueprint generated via Tier 1 ($modelName) in ${System.currentTimeMillis() - startTime}ms")
                    return cloudResult
                }
            } catch (e: Exception) {
                android.util.Log.w("StyleSimulatorEngine", "THINKING: Tier 1 fallback ($modelName) failed (${e.message})")
            }
        }

        // Tier 2: Deterministic (Local Heuristics) - Final Safety Net
        android.util.Log.d("StyleSimulatorEngine", "THINKING: Attempting Tier 2 (Local Heuristic Architect)...")
        val localResult = architectLocalBlueprint(userIntent, availableWardrobe)
        android.util.Log.d("StyleSimulatorEngine", "SUCCESS: Blueprint generated via Tier 2 in ${System.currentTimeMillis() - startTime}ms")
        return localResult
    }

    private suspend fun architectCloudBlueprint(
        prompt: String,
        userPortrait: android.graphics.Bitmap?,
        apiKey: String,
        modelName: String
    ): StyleBlueprint? {
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
            userPortrait?.let { image(it) }
            text(prompt)
        }

        val response = generativeModel.generateContent(inputContent)
        val jsonText = response.text ?: return null
        return sanitizeAndDecode(jsonText)
    }

    private fun minifyManifest(
        wardrobe: List<ClothingItem>,
        cosmetics: List<CosmeticItem>,
        anchoredWardrobe: List<ClothingItem>,
        anchoredCosmetics: List<CosmeticItem>
    ): String {
        // Pruning: If a category is anchored, only include those items.
        val anchoredCategories = anchoredWardrobe.map { it.category }.toSet()
        val prunedWardrobe = wardrobe.filter { item ->
            if (anchoredCategories.contains(item.category)) {
                anchoredWardrobe.any { it.id == item.id }
            } else true
        }

        val anchoredCosmeticMacros = anchoredCosmetics.map { it.macroCategory }.toSet()
        val prunedCosmetics = cosmetics.filter { item ->
            if (anchoredCosmeticMacros.contains(item.macroCategory)) {
                anchoredCosmetics.any { it.id == item.id }
            } else true
        }

        // Mapping to Lightweight DTOs
        val minWardrobe = prunedWardrobe.groupBy { it.category.name.lowercase() }
            .mapValues { (_, items) ->
                items.distinctBy { "${it.category}_${it.colorFamily}" } // Deduplication
                    .map { MinifiedClothing(it.id.toString(), it.name.lowercase(), it.colorHex ?: "unknown", it.formality.name.lowercase()) }
            }

        val minCosmetics = prunedCosmetics.groupBy { it.macroCategory.name.lowercase() }
            .mapValues { (_, items) ->
                items.distinctBy { "${it.microCategory}_${it.colorFamily}" }
                    .map { MinifiedCosmetic(it.id.toString(), it.name.lowercase(), it.colorHex ?: "unknown") }
            }

        return json.encodeToString(CloudManifest(minWardrobe, minCosmetics))
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
            You are the KoColor Style Architect AI. Generate a "Style Blueprint" that is both stylistically harmonic and biologically protective.
            
            USER INTENT: $userIntent
            BIOLOGICAL CONTEXT: $circadianContext (Wellness: ${"%.2f".format(wellnessScore)}, Ritual Done: $routineCompleted)
            WEATHER/ATMOSPHERIC: $weatherContext
            SKIN PROFILE: ${fashionProfile ?: "Unknown"}
            
            IMAGE DATA: I have provided a portrait of the user. Use this as the source of truth for their visual canvas.
            
            AVAILABLE VAULT (MINIFIED MANIFEST):
            $minifiedManifest
            
            GOAL:
            1. Select BEST 3 clothing items (Top, Bottom, Shoes) from the manifest.
            2. Select exactly 3 PIGMENT makeup items (1 Eye, 1 Cheek, 1 Lip) from the manifest.
            3. Select 1-2 DEFENSIVE items (Complexion/Skincare) from the manifest based strictly on the WEATHER/ATMOSPHERIC data. 
               - If UV is high, select an SPF product.
               - If humidity/heat is high, select a matte/long-wear foundation or primer.
            4. Create a 3-color Palette (HEX codes) harmonizing the whole look.
            5. Provide a brief rationale. Mention WHY you selected the specific DEFENSIVE items for the current weather.
            
            Respond ONLY with a valid JSON object matching this schema:
            {
              "rationale": "string",
              "selectedClothingIds": [Long, Long, Long],
              "selectedCosmeticIds": [Long, Long, Long, ...],
              "recommendedPalette": ["#HEX", "#HEX", "#HEX"]
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
        availableWardrobe: List<ClothingItem>
    ): StyleBlueprint {
        val selectedItems = mutableListOf<ClothingItem>()
        
        // Simple heuristic selection based on categories
        val tops = availableWardrobe.filter { it.category == ClothingCategory.TOPS }
        val bottoms = availableWardrobe.filter { it.category == ClothingCategory.BOTTOMS }
        val shoes = availableWardrobe.filter { it.category == ClothingCategory.SHOES }
        
        fun List<ClothingItem>.smartPick(): ClothingItem? {
            if (this.isEmpty()) return null
            val matches = this.filter { item ->
                userIntent.split(" ").any { keyword -> 
                    item.name.contains(keyword, ignoreCase = true) || 
                    (item.notes?.contains(keyword, ignoreCase = true) ?: false)
                }
            }
            return matches.randomOrNull() ?: this.random()
        }

        tops.smartPick()?.let { selectedItems.add(it) }
        bottoms.smartPick()?.let { selectedItems.add(it) }
        shoes.smartPick()?.let { selectedItems.add(it) }
        
        val accessories = availableWardrobe.filter { it.category == ClothingCategory.ACCESSORIES }.shuffled().take(2)
        selectedItems.addAll(accessories)

        val palette = selectedItems.mapNotNull { it.dominantHex }.distinct().toMutableList()
        if (palette.isEmpty()) palette.add("#FFFFFF")
        
        while (palette.size < 3) {
            palette.add(listOf("#000000", "#808080", "#E0E0E0", "#333333").random())
        }
        val finalPalette = palette.take(3)

        return StyleBlueprint(
            rationale = "Local Architect: Selected from your vault based on intent.",
            selectedClothingIds = selectedItems.map { it.id },
            selectedCosmeticIds = emptyList(),
            recommendedPalette = finalPalette
        )
    }
}

@Serializable
data class StyleBlueprint(
    val rationale: String,
    val selectedClothingIds: List<Long>,
    val selectedCosmeticIds: List<Long>,
    val recommendedPalette: List<String>
)

@Serializable
private data class MinifiedClothing(
    val id: String,
    val type: String,
    val hex: String,
    val vibe: String
)

@Serializable
private data class MinifiedCosmetic(
    val id: String,
    val type: String,
    val hex: String
)

@Serializable
private data class CloudManifest(
    val wardrobe: Map<String, List<MinifiedClothing>>,
    val cosmetics: Map<String, List<MinifiedCosmetic>>
)
