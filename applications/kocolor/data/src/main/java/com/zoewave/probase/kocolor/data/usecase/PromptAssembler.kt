package com.zoewave.probase.kocolor.data.usecase

import com.zoewave.probase.features.ai.core.AiInput
import com.zoewave.probase.features.ai.core.AiProviderCapability
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PromptAssembler @Inject constructor() {

    fun buildExactRequest(
        context: StyleRequestContext,
        compactManifest: String,
        providerCapability: AiProviderCapability
    ): AiInput {
        val profile = context.appearanceProfile
        val clothingGoal = if (compactManifest.contains("SHOES", ignoreCase = true)) {
            "1. Select BEST 3 clothing items (1 Top, 1 Bottom, 1 Shoes) from the WARDROBE section."
        } else {
            "1. Select BEST 2 clothing items (1 Top, 1 Bottom) from the WARDROBE section."
        }

        val cosmeticCategories = mutableListOf<String>()
        if (compactManifest.contains("EYES", ignoreCase = true)) cosmeticCategories.add("Eye")
        if (compactManifest.contains("DIMENSION", ignoreCase = true) || compactManifest.contains("CHEEK", ignoreCase = true)) cosmeticCategories.add("Cheek")
        if (compactManifest.contains("LIPS", ignoreCase = true)) cosmeticCategories.add("Lip")
        if (compactManifest.contains("NAILS", ignoreCase = true)) cosmeticCategories.add("Nail")

        val cosmeticGoal = if (cosmeticCategories.isNotEmpty()) {
            "2. Select 1 item from each available cosmetic category (${cosmeticCategories.joinToString(", ")}) from the COSMETICS section."
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
