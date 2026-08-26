package com.zoewave.probase.kocolor.data.usecase

import com.zoewave.probase.features.ai.core.StylePromptRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PromptAssembler @Inject constructor() {

    /**
     * Combines system instructions, telemetry, context, and the compact manifest into a final prompt.
     */
    fun buildExactRequest(
        context: StyleRequestContext,
        compactManifest: String
    ): StylePromptRequest {
        val prompt = """
            You are the KoColor Style Architect AI. Generate a "Style Blueprint" that is both stylistically harmonic and protective.
            
            APPEARANCE TELEMETRY: ${context.appearanceTelemetry}
            WEATHER/ATMOSPHERIC: ${context.weather}
            CIRCADIAN CONTEXT: ${context.circadianContext} (Wellness Score: ${context.wellnessScore})
            USER INTENT: ${context.intent}
            
            AVAILABLE CANDIDATES (COMPACT MANIFEST):
            $compactManifest
            
            GOAL:
            1. Select BEST 3 clothing items (Top, Bottom, Shoes) from the provided manifest.
            2. construct a harmonic style including rationale.
            
            Respond ONLY with a valid JSON object matching this schema:
            {
              "rationale": "string",
              "selectedClothingIds": ["w_id", "w_id", "w_id"],
              "selectedCosmeticIds": ["c_id", ...],
              "recommendedPalette": ["#HEX", "#HEX", "#HEX", "#HEX"]
            }
        """.trimIndent()
        
        return StylePromptRequest(prompt)
    }
}
