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
        val prompt = """
            You are the KoColor Style Architect AI. Generate a "Style Blueprint" that is both stylistically harmonic and protective.
            
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
            1. Select BEST 3 clothing items (Top, Bottom, Shoes) from the WARDROBE section.
            2. Select exactly 4 COSMETIC items (1 Eye, 1 Cheek, 1 Lip, 1 Nail) from the COSMETICS section. 
            3. Construct a harmonic style where all colors work together, including a rationale.
            
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
