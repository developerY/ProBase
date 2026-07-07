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
          "selectedCosmeticIds": ["Long", "Long", "Long", "..."],
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
        
        val prompt = buildArchitectPrompt(
            userIntent, circadianContext, routineCompleted, wellnessScore, weatherContext, 
            availableWardrobe, availableCosmetics, fashionProfile, anchoredClothing, anchoredCosmetics
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
            android.util.Log.d("StyleSimulatorEngine", "THINKING: Attempting Tier 1 (Cloud Gemini 1.5 Flash Fallback)...")
            try {
                val cloudResult = architectCloudBlueprint(prompt, userPortrait, apiKey, modelName)
                if (cloudResult != null) {
                    android.util.Log.d("StyleSimulatorEngine", "SUCCESS: Blueprint generated via Tier 1 in ${System.currentTimeMillis() - startTime}ms")
                    return cloudResult
                }
            } catch (e: Exception) {
                android.util.Log.w("StyleSimulatorEngine", "THINKING: Tier 1 fallback failed (${e.message})")
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
        val generativeModel = GenerativeModel(
            modelName = modelName,
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

    private fun buildArchitectPrompt(
        userIntent: String,
        circadianContext: String,
        routineCompleted: Boolean,
        wellnessScore: Double,
        weatherContext: String,
        availableWardrobe: List<ClothingItem>,
        availableCosmetics: List<CosmeticItem>,
        fashionProfile: String?,
        anchoredClothing: List<ClothingItem>,
        anchoredCosmetics: List<CosmeticItem>
    ): String {
        val wardrobeDescription = availableWardrobe.joinToString("\n") { 
            "CLOTHING_ID: ${it.id}, Name: ${it.name}, Category: ${it.category}, Color: ${it.colorHex ?: "Unknown"}"
        }
        
        val cosmeticsDescription = availableCosmetics.joinToString("\n") {
            "COSMETIC_ID: ${it.id}, Name: ${it.name}, Brand: ${it.brand}, Category: ${it.microCategory}, Color: ${it.colorHex ?: "Unknown"}, Notes: ${it.notes ?: "None"}"
        }

        val anchorContext = StringBuilder()
        if (anchoredClothing.isNotEmpty()) {
            anchorContext.append("\nUSER'S MUST-INCLUDE CLOTHING:\n")
            anchoredClothing.forEach { anchorContext.append("- CLOTHING_ID: ${it.id}, Name: ${it.name}\n") }
        }
        if (anchoredCosmetics.isNotEmpty()) {
            anchorContext.append("\nUSER'S MUST-INCLUDE COSMETICS:\n")
            anchoredCosmetics.forEach { anchorContext.append("- COSMETIC_ID: ${it.id}, Name: ${it.name}\n") }
        }

        return """
            You are the KoColor Style Architect AI. Generate a "Style Blueprint" that is both stylistically harmonic and biologically protective.
            
            USER INTENT: $userIntent
            BIOLOGICAL CONTEXT: $circadianContext (Wellness: ${"%.2f".format(wellnessScore)}, Ritual Done: $routineCompleted)
            WEATHER/ATMOSPHERIC: $weatherContext
            SKIN PROFILE: ${fashionProfile ?: "Unknown"}
            
            $anchorContext
            
            IMAGE DATA: I have provided a portrait of the user. Use this as the source of truth for their visual canvas.
            
            AVAILABLE VAULT (USE THESE EXACT ITEMS):
            --- WARDROBE ---
            $wardrobeDescription
            
            --- COSMETIC VANITY ---
            $cosmeticsDescription
            
            GOAL:
            1. Select BEST 3 clothing items (Top, Bottom, Shoes). Prioritize user anchors.
            2. Select exactly 3 PIGMENT items (1 Eye, 1 Cheek, 1 Lip) from the COSMETIC VANITY. Prioritize user anchors.
            3. Select 1-2 DEFENSIVE items (Complexion/Skincare) from the COSMETIC VANITY based strictly on the WEATHER/ATMOSPHERIC data. 
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
