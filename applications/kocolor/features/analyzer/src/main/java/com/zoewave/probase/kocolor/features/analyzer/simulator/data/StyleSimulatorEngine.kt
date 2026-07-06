package com.zoewave.probase.kocolor.features.analyzer.simulator.data

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.zoewave.probase.core.model.ritual.*
import com.zoewave.probase.features.ai.local.data.LocalAiEngine
import com.zoewave.probase.features.ai.local.domain.router.RequiresCloudException
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
          "selectedItemIds": ["Long", "Long", ...],
          "recommendedPalette": ["#HEX", "#HEX", "#HEX"]
        }
    """.trimIndent()

    suspend fun architectStyleBlueprint(
        userIntent: String,
        circadianContext: String,
        routineCompleted: Boolean,
        wellnessScore: Double,
        availableWardrobe: List<ClothingItem>,
        fashionProfile: String? = null,
        userPortrait: android.graphics.Bitmap? = null,
        apiKey: String? = null,
        modelName: String = "gemini-1.5-flash"
    ): StyleBlueprint {
        
        val prompt = buildArchitectPrompt(
            userIntent, circadianContext, routineCompleted, wellnessScore, availableWardrobe, fashionProfile
        )

        // Tier 1: Probabilistic (Cloud Gemini BYOK Multimodal)
        if (!apiKey.isNullOrBlank()) {
            try {
                val cloudResult = architectCloudBlueprint(prompt, userPortrait, apiKey, modelName)
                if (cloudResult != null) return cloudResult
            } catch (e: Exception) {
                android.util.Log.w("StyleSimulatorEngine", "Tier 1 (Cloud) failed, falling back to Tier 1.5 (Nano)")
            }
        }

        // Tier 1.5: Local LLM (Gemini Nano)
        try {
            // Note: If userPortrait is present, Nano path may require multimodal-specific routing
            val localAiResult = localAi.generateStructuredContent(prompt, BLUEPRINT_SCHEMA)
            localAiResult.onSuccess { jsonText ->
                return sanitizeAndDecode(jsonText)
            }
        } catch (e: RequiresCloudException) {
            android.util.Log.d("StyleSimulatorEngine", "Tier 1.5 (Nano) bypass: ${e.message}")
        } catch (e: Exception) {
            android.util.Log.e("StyleSimulatorEngine", "Tier 1.5 (Nano) failed", e)
        }

        // Tier 2: Deterministic (Local Heuristics)
        return architectLocalBlueprint(userIntent, availableWardrobe)
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
        availableWardrobe: List<ClothingItem>,
        fashionProfile: String?
    ): String {
        val wardrobeDescription = availableWardrobe.joinToString("\n") { 
            "ID: ${it.id}, Name: ${it.name}, Category: ${it.category}, Color: ${it.colorHex ?: "Unknown"}"
        }

        return """
            You are the KoColor Style Architect AI. Generate a "Style Blueprint".
            
            USER INTENT: $userIntent
            BIOLOGICAL CONTEXT: $circadianContext
            SKIN PROFILE (TEXT): ${fashionProfile ?: "Unknown"}
            MORNING RITUAL COMPLETED: $routineCompleted
            WELLNESS SCORE: ${"%.2f".format(wellnessScore)}
            
            IMAGE DATA: I have provided a portrait of the user. Use this image as the PRIMARY source of truth for their complexion, eye color, and hair tone.
            
            AVAILABLE WARDROBE (PRE-FILTERED):
            $wardrobeDescription
            
            GOAL:
            1. Select BEST 3 items (Top, Bottom, Shoes).
            2. Recommend 2 Accessories.
            3. Create a 3-color Palette (HEX codes) harmonizing with items and the user's VISUAL features.
            4. Provide a brief rationale.
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
        
        // 1. Simple heuristic selection based on categories
        val tops = availableWardrobe.filter { it.category == ClothingCategory.TOPS }
        val bottoms = availableWardrobe.filter { it.category == ClothingCategory.BOTTOMS }
        val shoes = availableWardrobe.filter { it.category == ClothingCategory.SHOES }
        
        // Try to match intent keywords with item names
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

        // 2. Generate palette from selected items
        val palette = selectedItems.mapNotNull { it.dominantHex }.distinct().toMutableList()
        if (palette.isEmpty()) palette.add("#FFFFFF")
        
        // Pad with harmonized neutrals if needed
        while (palette.size < 3) {
            palette.add(listOf("#000000", "#808080", "#E0E0E0", "#333333").random())
        }
        val finalPalette = palette.take(3)

        val itemsNames = selectedItems.joinToString(", ") { it.name }
        
        return StyleBlueprint(
            rationale = "Local Architect: Selected a curated set from your vault ($itemsNames) that best aligns with your stated intent. We've anchored the look with a balanced palette derived from your collection.",
            selectedItemIds = selectedItems.map { it.id },
            recommendedPalette = finalPalette
        )
    }
}

@Serializable
data class StyleBlueprint(
    val rationale: String,
    val selectedItemIds: List<Long>,
    val recommendedPalette: List<String>
)
