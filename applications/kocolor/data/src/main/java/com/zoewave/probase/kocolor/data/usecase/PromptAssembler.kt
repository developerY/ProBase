package com.zoewave.probase.kocolor.data.usecase

import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.MacroCategory
import com.zoewave.probase.features.ai.core.AiInput
import com.zoewave.probase.features.ai.core.AiProviderCapability
import com.zoewave.probase.kocolor.data.color.CandidateProvenance
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PromptAssembler @Inject constructor() {

    fun buildExactRequest(
        context: StyleRequestContext,
        compactManifest: String,
        clothingCandidates: List<CandidateProvenance> = emptyList(),
        cosmeticCandidates: List<CandidateProvenance> = emptyList(),
        providerCapability: AiProviderCapability
    ): AiInput {
        val profile = context.appearanceProfile

        val availableClothingCategories = clothingCandidates.mapNotNull { it.clothingItem?.category }.toSet()
        val hasShoes = availableClothingCategories.contains(ClothingCategory.SHOES) || (clothingCandidates.isEmpty() && compactManifest.contains("SHOES", ignoreCase = true))

        val clothingGoal = if (hasShoes) {
            "1. Select BEST 3 clothing items (1 Top, 1 Bottom, 1 Shoes) from the WARDROBE section."
        } else {
            "1. Select BEST 2 clothing items (1 Top, 1 Bottom) from the WARDROBE section."
        }

        val availableCosmeticCategories = cosmeticCandidates.mapNotNull { it.cosmeticItem?.macroCategory }.toSet()
        val cosmeticCategories = mutableListOf<String>()
        if (cosmeticCandidates.isEmpty() || availableCosmeticCategories.contains(MacroCategory.EYES) || compactManifest.contains("EYES", ignoreCase = true)) cosmeticCategories.add("Eye")
        if (cosmeticCandidates.isEmpty() || availableCosmeticCategories.contains(MacroCategory.DIMENSION) || compactManifest.contains("CHEEK", ignoreCase = true) || compactManifest.contains("DIMENSION", ignoreCase = true)) cosmeticCategories.add("Cheek")
        if (cosmeticCandidates.isEmpty() || availableCosmeticCategories.contains(MacroCategory.LIPS) || compactManifest.contains("LIPS", ignoreCase = true)) cosmeticCategories.add("Lip")
        if (cosmeticCandidates.isEmpty() || availableCosmeticCategories.contains(MacroCategory.NAILS) || compactManifest.contains("NAILS", ignoreCase = true)) cosmeticCategories.add("Nail")

        val cosmeticGoal = if (cosmeticCategories.isNotEmpty()) {
            "2. Select 1 item from each available cosmetic role (${cosmeticCategories.joinToString(", ")}) from the COSMETICS section."
        } else {
            "2. Select available cosmetic items from the COSMETICS section."
        }

        val prompt = """
            You are the KoColor Style Architect AI. Generate a "Style Blueprint" that is both stylistically harmonic and protective.
            
            STRICT GROUNDING RULE:
            Do not invent stylistic adjectives (e.g., do not call nylon 'structural'). Describe items strictly using the physical materials and attributes listed in the manifest.
            
            APPEARANCE TELEMETRY:
            - Temperature: ${profile.undertone}
            - Depth: ${profile.depth}
            - Contrast: ${profile.contrast}
            
            WEATHER/ATMOSPHERIC: ${context.weather} (Temp: ${context.weatherTempC}°C, UV: ${context.uvIndex})
            CIRCADIAN CONTEXT: ${context.circadianContext} (Wellness Score: ${context.wellnessScore})
            USER INTENT: ${context.intent}
            OCCASION: ${context.occasion}
            
            AVAILABLE CANDIDATES (COMPACT MANIFEST):
            $compactManifest
            
            GOAL:
            $clothingGoal
            $cosmeticGoal
            3. Construct a harmonic style where all colors work together, including a rationale referencing ONLY selected item IDs.
            
            Respond ONLY with a valid JSON object matching this schema:
            {
              "rationale": "string",
              "selectedClothingIds": ["w_id", "w_id", "w_id"],
              "selectedCosmeticIds": ["c_id", "c_id", "c_id", "c_id"],
              "recommendedPalette": ["#HEX", "#HEX", "#HEX", "#HEX"]
            }
        """.trimIndent()

        val bitmap = context.localImageBitmap
        return if (providerCapability.supportsLocalImageIngestion && bitmap != null) {
            AiInput.Multimodal(promptString = prompt, localImage = bitmap)
        } else {
            AiInput.TextOnly(promptString = prompt)
        }
    }
}
