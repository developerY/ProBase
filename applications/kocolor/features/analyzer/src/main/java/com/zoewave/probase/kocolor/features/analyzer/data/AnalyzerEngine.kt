package com.zoewave.probase.kocolor.features.analyzer.data

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.zoewave.probase.kocolor.model.FashionAdvice
import com.zoewave.probase.kocolor.model.CosmeticItem
import com.zoewave.probase.kocolor.model.CosmeticCategory
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
        shoes: Bitmap?,
        clothes: Bitmap?,
        occasion: String,
        location: String?,
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
            shoes?.let { image(it) }
            clothes?.let { image(it) }
            
            text("""
                You are a professional personal color analyst and style consultant. 
                The user is preparing for a specific occasion: $occasion.
                ${if (location != null) "Current Location Context: $location. Take local climate and fashion trends for this area into account." else ""}
                
                I have provided up to 4 images:
                1. Face selfie
                2. Hair photo
                3. Shoes photo
                4. Clothing/Outfit photo
                
                GOAL:
                Analyze the provided physical attributes (face, hair) and coordinate them with the outfit (clothes, shoes) to recommend a perfect makeup and nail polish palette TAILORED for the occasion: $occasion.
                
                OCCASION GUIDANCE:
                - If "Work": Lean toward sophisticated, professional neutrals and polished finishes.
                - If "Date Night": Tilt toward romantic, bold, or glamorous shades with alluring finishes.
                - If "Outdoor/Sport": Focus on fresh, natural, and durable looks.
                - If "Formal": Aim for high-fidelity elegance, classic harmonies, and refined palettes.
                
                TASKS:
                1. Identify the Seasonal Type (SPRING, SUMMER, AUTUMN, WINTER) of the face.
                2. Identify the Undertone (WARM, COOL, NEUTRAL) of the face.
                3. Analyze how the hair and shoe colors (if provided) interact with the skin tone and clothing.
                4. Provide a cohesive summary explaining the coordination strategy for the $occasion ${if (location != null) "in $location" else ""}.
                5. Give specific makeup suggestions (Foundation, Lip, Eye, Blush) appropriate for $occasion.
                6. Give a specific recommendation for NAIL POLISH color and finish that ties the whole look together for $occasion. Ensure this is categorized as "Nail Polish" in your response.
                7. Recommend a makeup color palette (HEX codes).
                
                Respond ONLY with a valid JSON object matching this exact schema:
                {
                  "summary": "string",
                  "seasonalType": "SPRING" | "SUMMER" | "AUTUMN" | "WINTER",
                  "undertone": "WARM" | "COOL" | "NEUTRAL",
                  "makeupSuggestions": [
                    { "category": "string", "advice": "string", "recommendedColors": ["string"] }
                  ],
                  "outfitSuggestions": [
                    { "occasion": "$occasion", "advice": "string", "keyPieces": ["string"], "colorCombinations": ["string"] }
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

    suspend fun analyzeCosmeticProduct(
        image: Bitmap,
        apiKey: String,
        modelName: String = "gemini-1.5-flash"
    ): CosmeticItem? {
        val generativeModel = GenerativeModel(
            modelName = modelName,
            apiKey = apiKey,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
            }
        )

        val prompt = content {
            image(image)
            text("""
                Identify this cosmetic product from the image. 
                Extract the product name, brand, shade name (if visible), and the most prominent color (as a HEX code).
                Also, categorize the product into one of the following categories:
                ${CosmeticCategory.entries.filter { it != CosmeticCategory.AI_PENDING }.joinToString(", ") { it.name }}
                
                Respond ONLY with a valid JSON object matching this exact schema:
                {
                  "name": "string",
                  "brand": "string",
                  "category": "string (one of the enum values provided)",
                  "shadeName": "string",
                  "colorHex": "#HEX",
                  "notes": "string (brief description of product features)"
                }
            """.trimIndent())
        }

        return try {
            val response = generativeModel.generateContent(prompt)
            val jsonText = response.text ?: return null
            val cleanedJson = jsonText.substringAfter("{").substringBeforeLast("}")
            val finalJson = "{$cleanedJson}"
            
            val result = json.decodeFromString<CosmeticItemJson>(finalJson)
            CosmeticItem(
                name = result.name,
                brand = result.brand,
                category = CosmeticCategory.valueOf(result.category),
                colorHex = result.colorHex,
                shadeName = result.shadeName,
                notes = result.notes
            )
        } catch (e: Exception) {
            null
        }
    }
}

@kotlinx.serialization.Serializable
private data class CosmeticItemJson(
    val name: String,
    val brand: String,
    val category: String,
    val shadeName: String? = null,
    val colorHex: String? = null,
    val notes: String? = null
)
