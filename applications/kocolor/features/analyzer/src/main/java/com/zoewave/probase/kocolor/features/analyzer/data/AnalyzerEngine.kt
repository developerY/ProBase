package com.zoewave.probase.kocolor.features.analyzer.data

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.zoewave.probase.kocolor.model.FashionAdvice
import com.zoewave.probase.kocolor.model.SeasonalType
import com.zoewave.probase.kocolor.model.Undertone
import kotlinx.serialization.json.Json
import javax.inject.Inject

class AnalyzerEngine @Inject constructor() {

    private val json = Json { 
        ignoreUnknownKeys = true 
        coerceInputValues = true
    }

    suspend fun analyzeStyle(
        face: Bitmap?,
        hair: Bitmap?,
        nail: Bitmap?,
        clothes: Bitmap?,
        apiKey: String,
        modelName: String = "gemini-1.5-flash"
    ): FashionAdvice {
        val generativeModel = GenerativeModel(
            modelName = modelName,
            apiKey = apiKey,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
            }
        )

        val prompt = content {
            face?.let { image(it) }
            hair?.let { image(it) }
            nail?.let { image(it) }
            clothes?.let { image(it) }
            
            text("""
                You are a professional personal color analyst, hair stylist, and makeup artist. 
                I have provided up to 4 images:
                1. Face selfie
                2. Hair photo
                3. Nail photo
                4. Clothing/Outfit photo
                
                GOAL:
                Analyze the provided physical attributes (skin, hair, nails) and coordinate them with the clothing to recommend a complete, high-fidelity fashion and makeup plan.
                
                TASKS:
                1. Identify the Seasonal Type (SPRING, SUMMER, AUTUMN, WINTER).
                2. Identify the Undertone (WARM, COOL, NEUTRAL).
                3. Analyze how the hair and nail colors (if provided) interact with the skin tone and outfit.
                4. Provide a cohesive summary of the look and how to achieve perfect harmony.
                5. Give specific makeup suggestions (Foundation, Lip, Eye, Blush).
                6. Recommend a makeup color palette (HEX codes) that ties everything together.
                
                Respond ONLY with a valid JSON object matching this exact schema:
                {
                  "summary": "string",
                  "seasonalType": "SPRING" | "SUMMER" | "AUTUMN" | "WINTER",
                  "undertone": "WARM" | "COOL" | "NEUTRAL",
                  "makeupSuggestions": [
                    { "category": "string", "advice": "string", "recommendedColors": ["string"] }
                  ],
                  "outfitSuggestions": [
                    { "occasion": "Coordinated Look", "advice": "string", "keyPieces": ["string"], "colorCombinations": ["string"] }
                  ],
                  "recommendedPalette": ["#HEX", "#HEX", ...]
                }
            """.trimIndent())
        }

        return try {
            val response = generativeModel.generateContent(prompt)
            val jsonText = response.text ?: throw IllegalStateException("Empty response from AI")
            
            // Clean JSON string in case of LLM artifacts
            val cleanedJson = jsonText.substringAfter("{").substringBeforeLast("}")
            val finalJson = "{$cleanedJson}"
            
            json.decodeFromString<FashionAdvice>(finalJson)
        } catch (e: Exception) {
            FashionAdvice(
                summary = "Error during analysis: ${e.localizedMessage}",
                seasonalType = SeasonalType.UNKNOWN,
                undertone = Undertone.UNKNOWN,
                makeupSuggestions = emptyList(),
                outfitSuggestions = emptyList(),
                recommendedPalette = emptyList()
            )
        }
    }
}
