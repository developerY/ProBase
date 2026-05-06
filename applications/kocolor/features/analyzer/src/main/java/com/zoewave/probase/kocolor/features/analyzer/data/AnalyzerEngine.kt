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

    suspend fun analyzeFaceAndClothes(
        faceBitmap: Bitmap,
        clothesBitmap: Bitmap,
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
            image(faceBitmap)
            image(clothesBitmap)
            text("""
                You are a professional personal color analyst and makeup artist. 
                I have provided two images:
                1. A photo of a person's face.
                2. A photo of an outfit or clothing item they plan to wear.
                
                GOAL:
                Analyze the skin undertone and seasonal color of the face, and coordinate it with the colors in the clothing to recommend the PERFECT makeup color palette for this specific look.
                
                1. Identify the Seasonal Type (SPRING, SUMMER, AUTUMN, WINTER) of the face.
                2. Identify the Undertone (WARM, COOL, NEUTRAL) of the face.
                3. Provide a summary explaining how the recommended makeup coordinates the face with the clothes.
                4. Give specific makeup suggestions (Foundation, Lip, Eye, Blush).
                5. Recommend a color palette (HEX codes) for the makeup and overall coordination.
                
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
