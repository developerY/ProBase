package com.zoewave.probase.kocolor.features.suggestions.data

import com.zoewave.probase.features.ai.core.AiProvider
import com.zoewave.probase.features.ai.core.AiInput
import com.zoewave.probase.kocolor.data.usecase.CapabilityRouter
import com.zoewave.probase.core.model.ritual.FashionAdvice
import com.zoewave.probase.core.model.ritual.FashionProfile
import com.zoewave.probase.core.model.ritual.SeasonalType
import com.zoewave.probase.core.model.ritual.Undertone
import kotlinx.serialization.json.Json
import javax.inject.Inject

class SuggestionEngine @Inject constructor(
    private val capabilityRouter: CapabilityRouter
) {

    private val json = Json { 
        ignoreUnknownKeys = true 
        coerceInputValues = true
    }

    suspend fun getPersonalizedAdvice(
        profile: FashionProfile,
        query: String?
    ): FashionAdvice {
        return try {
            val textPrompt = """
                You are a professional personal fashion consultant. 
                Based on the user's profile, provide personalized fashion and makeup advice.
                
                USER PROFILE:
                - Seasonal Type: ${profile.seasonalType}
                - Undertone: ${profile.undertone}
                - Skin Tone: ${profile.skinToneHex ?: "Not specified"}
                - Eye Color: ${profile.eyeColor ?: "Not specified"}
                - Hair Color: ${profile.hairColor ?: "Not specified"}
                
                ${if (!query.isNullOrBlank()) "USER REQUEST: $query" else "GOAL: Provide a comprehensive update on makeup and clothing suggestions."}
                
                Respond ONLY with a valid JSON object matching this exact schema:
                {
                  "summary": "string",
                  "seasonalType": "SPRING" | "SUMMER" | "AUTUMN" | "WINTER",
                  "undertone": "WARM" | "COOL" | "NEUTRAL",
                  "makeupSuggestions": [
                    { "category": "string", "advice": "string", "recommendedColors": ["string"] }
                  ],
                  "outfitSuggestions": [
                    { "occasion": "string", "advice": "string", "keyPieces": ["string"], "colorCombinations": ["string"] }
                  ],
                  "recommendedPalette": ["#HEX", "#HEX", ...]
                }
            """.trimIndent()

            val provider = capabilityRouter.getRankedAvailableProviders().firstOrNull()
                ?: throw IllegalStateException("No AI providers available")

            val input = AiInput.TextOnly(promptString = textPrompt)
            val result = provider.execute(input)
            val jsonText = result.getOrNull()?.text ?: throw IllegalStateException("Empty response from AI")
            
            val cleanedJson = jsonText.substringAfter("{").substringBeforeLast("}")
            val finalJson = "{$cleanedJson}"
            
            json.decodeFromString<FashionAdvice>(finalJson)
        } catch (e: Exception) {
            FashionAdvice(
                summary = "Error getting suggestions: ${e.localizedMessage}",
                seasonalType = profile.seasonalType,
                undertone = profile.undertone,
                makeupSuggestions = emptyList(),
                outfitSuggestions = emptyList(),
                recommendedPalette = emptyList()
            )
        }
    }
}
