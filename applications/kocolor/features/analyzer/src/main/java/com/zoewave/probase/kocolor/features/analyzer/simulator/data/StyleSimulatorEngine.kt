package com.zoewave.probase.kocolor.features.analyzer.simulator.data

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.core.model.ritual.CosmeticItem
import com.zoewave.probase.core.model.ritual.Formality
import com.zoewave.probase.core.model.ritual.MacroCategory
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
          "selectedClothingIds": ["String", "String", "String"],
          "selectedCosmeticIds": ["String", "String", "String", ...],
          "recommendedPalette": ["#HEX", "#HEX", "#HEX", "#HEX"]
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
        
        // 1. Manifest Minification (Cloud-Optimization via Tuple Matrix)
        val minifiedManifest = minifyManifest(
            availableWardrobe, availableCosmetics, anchoredClothing, anchoredCosmetics
        )
        android.util.Log.d("StyleSimulatorEngine", "DATA_OUT (Minified Manifest): $minifiedManifest")

        val prompt = buildArchitectPrompt(
            userIntent, circadianContext, routineCompleted, wellnessScore, weatherContext, 
            minifiedManifest, fashionProfile
        )
        android.util.Log.d("StyleSimulatorEngine", "DATA_OUT (Full Prompt):\n$prompt")

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
                android.util.Log.e("StyleSimulatorEngine", "THINKING: Tier 1 fallback ($modelName) failed", e)
            }
        }

        // Tier 2: Deterministic (Local Heuristics) - Final Safety Net
        android.util.Log.d("StyleSimulatorEngine", "THINKING: Attempting Tier 2 (Local Heuristic Architect)...")
        val localResult = architectLocalBlueprint(userIntent, availableWardrobe, availableCosmetics)
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
        android.util.Log.d("StyleSimulatorEngine", "DATA_IN (Cloud Response Raw): $jsonText")
        return sanitizeAndDecode(jsonText)
    }

    private fun minifyManifest(
        wardrobe: List<ClothingItem>,
        cosmetics: List<CosmeticItem>,
        anchoredWardrobe: List<ClothingItem>,
        anchoredCosmetics: List<CosmeticItem>
    ): String {
        // Vibe Key: 0=casual, 1=professional, 2=gala, 3=smart-casual, 4=formal, 5=lounge
        fun Formality.toKey(): String = when(this) {
            Formality.CASUAL -> "0"
            Formality.PROFESSIONAL -> "1"
            Formality.GALA -> "2"
            Formality.SMART_CASUAL -> "3"
            Formality.FORMAL -> "4"
            Formality.LOUNGE -> "5"
        }

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

        // Mapping to Lightweight Matrix Tuples [ID, Type, Hex, VibeKey]
        val minWardrobe = prunedWardrobe.groupBy { it.category.name.lowercase() }
            .mapValues { (_, items) ->
                items.distinctBy { "${it.category}_${it.colorFamily}" }
                    .map { listOf("w_${it.id}", it.name.lowercase(), it.colorHex ?: "#000000", it.formality.toKey()) }
            }

        val minCosmetics = prunedCosmetics.groupBy { it.macroCategory.name.lowercase() }
            .mapValues { (_, items) ->
                items.distinctBy { "${it.microCategory}_${it.colorFamily}" }
                    .map { listOf("c_${it.id}", it.microCategory.name.lowercase(), it.colorHex ?: "#000000") }
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
            
            AVAILABLE VAULT (MATRIX REPRESENTATION):
            Legend:
            - Clothing Schema: [ID, Name/Type, ColorHex, VibeKey]
            - VibeKey: 0=casual, 1=professional, 2=gala, 3=smart-casual, 4=formal, 5=lounge
            - Cosmetic Schema: [ID, MicroCategory, ColorHex]
            - IDs: Prefixed with 'w_' for Wardrobe and 'c_' for Cosmetics.
            
            $minifiedManifest
            
            GOAL:
            1. Select BEST 3 clothing items (Top, Bottom, Shoes) from the wardrobe section of the manifest. Prioritize user anchors.
            2. Select exactly 4 PIGMENT makeup items (1 Eye, 1 Cheek, 1 Lip, 1 Nail) strictly from the cosmetics section of the manifest. Prioritize user anchors.
            3. Select 1-2 DEFENSIVE items (Complexion/Skincare) from the cosmetics section based strictly on the WEATHER/ATMOSPHERIC data. 
               - If UV is high, select an SPF product.
               - If humidity/heat is high, select a matte/long-wear foundation or primer.
            4. Create a 4-color Palette (HEX codes) harmonizing the whole look. The 4th color MUST be the selected Nail color.
            5. Provide a brief rationale. Mention WHY you selected the specific DEFENSIVE items for the current weather and why you chose the nail color.
               CRITICAL SYNTAX RULE: When referencing ANY selected item in your rationale, you MUST use the exact inline tag format <ITEM:id>. Do not attempt to guess or describe the item's brand in the text.
            
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

        return StyleBlueprint(
            rationale = "Local Architect: Selected from your vault based on intent.",
            selectedClothingIds = selectedItems.map { "w_${it.id}" },
            selectedCosmeticIds = selectedCosmetics.map { "c_${it.id}" },
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
