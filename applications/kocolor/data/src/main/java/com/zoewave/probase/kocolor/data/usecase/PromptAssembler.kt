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

        val cosmeticCategories = cosmeticCandidates.mapNotNull { prov ->
            prov.cosmeticItem?.let { CosmeticRole.fromMacroCategory(it.macroCategory)?.displayName }
        }.filter { it != "Prep" }.distinct()

        val activeCategories = if (cosmeticCategories.isNotEmpty()) {
            cosmeticCategories
        } else {
            listOf("Eye", "Cheek", "Lip", "Nail")
        }

        val cosmeticGoal = "2. Select 1 item from each available cosmetic role (${activeCategories.joinToString(", ")}) from the COSMETICS section."

        val lockedAnchors = clothingCandidates.filter {
            it.retrievalReason.contains("LOCKED ANCHOR", ignoreCase = true) ||
            it.retrievalReason.contains("INTENT ANCHOR", ignoreCase = true)
        }.distinctBy { it.id }.take(1)
        val anchorInstruction = if (lockedAnchors.isNotEmpty()) {
            val anchorListText = lockedAnchors.joinToString(", ") { prov ->
                val item = prov.clothingItem
                "item w_${item?.internalId} (\"${item?.name}\")"
            }
            "0. MANDATORY OUTFIT ANCHOR: You MUST include $anchorListText in your selectedClothingIds array."
        } else {
            ""
        }

        val prompt = """
            You are the KoColor Style Architect AI. Generate a "Style Blueprint" that is both stylistically harmonic and protective.
            
            STRICT GROUNDING RULES & CONSTRAINTS:
            1. DESCRIPTIVE ACCURACY: Do not invent stylistic adjectives (e.g., do not call nylon 'structural'). Describe items strictly using the physical materials and attributes listed in the manifest.
            2. CATEGORY ISOLATION: You may ONLY select cosmetics from the requested roles (Eye, Cheek, Lip, Nail). You are STRICTLY FORBIDDEN from selecting or referencing items categorized as PREP, HAIR, or COMPLEXION, regardless of the environmental context.
            3. RATIONALE FORMATTING: Write the rationale as fluid prose. Do not use decimals or decimal numbers (e.g., write "high UV" instead of "6.9 UV") to ensure clean downstream text processing.
            
            APPEARANCE TELEMETRY:
            - Temperature: ${profile.undertone}
            - Depth: ${profile.depth}
            - Contrast: ${profile.contrast}
            
            WEATHER/ATMOSPHERIC: Temp: ${context.weatherTempC ?: 22.0f}°C, UV: ${context.uvIndex ?: 3.0f}
            CIRCADIAN CONTEXT: ${context.circadianContext} (Wellness Score: ${context.wellnessScore})
            USER INTENT: ${context.intent}
            OCCASION: ${context.occasion}
            
            WARDROBE INTELLIGENCE:
            - Wardrobe DNA: ${context.wardrobeDna ?: "Balanced"}
            - Rotation Health Score: ${context.rotationHealth ?: 100}/100
            - Anchor Versatility Target: ${context.wardrobeVersatility ?: 0} compatible looks
            
            AVAILABLE CANDIDATES (COMPACT MANIFEST):
            $compactManifest
            
            GOAL:
            $anchorInstruction
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

        val tempOverride = if (context.intent.equals("Surprise Me", ignoreCase = true) || context.intentProfile.colorfulness >= 0.85f) {
            0.85f
        } else {
            null
        }

        val bitmap = context.localImageBitmap
        return if (providerCapability.supportsLocalImageIngestion && bitmap != null) {
            AiInput.Multimodal(promptString = prompt, localImage = bitmap, temperatureOverride = tempOverride)
        } else {
            AiInput.TextOnly(promptString = prompt, temperatureOverride = tempOverride)
        }
    }
}
