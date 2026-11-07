package com.zoewave.probase.kocolor.features.analyzer.data

import android.graphics.Bitmap
import com.zoewave.probase.core.model.ritual.*
import com.zoewave.probase.core.util.color.ColorQuantizer
import com.zoewave.probase.features.ai.core.AiInput
import com.zoewave.probase.kocolor.data.usecase.CapabilityRouter
import com.zoewave.probase.kocolor.fashionista.math.ItaCalculator
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyzerEngine @Inject constructor(
    private val capabilityRouter: CapabilityRouter,
    private val itaCalculator: ItaCalculator
) {

    private val json = Json { 
        ignoreUnknownKeys = true 
        coerceInputValues = true
    }

    private fun downscale(bitmap: Bitmap?): Bitmap? {
        if (bitmap == null) return null
        val maxEdge = 1024
        val width = bitmap.width
        val height = bitmap.height
        if (width <= maxEdge && height <= maxEdge) return bitmap
        val ratio = maxEdge.toFloat() / maxOf(width, height)
        return Bitmap.createScaledBitmap(bitmap, (width * ratio).toInt(), (height * ratio).toInt(), true)
    }

    private fun extractFaceTelemetryHex(face: Bitmap?): String? {
        if (face == null) return null
        return try {
            val pixel = face.getPixel(face.width / 2, face.height / 2)
            String.format("#%06X", (0xFFFFFF and pixel))
        } catch (e: Exception) {
            null
        }
    }

    suspend fun analyzeStyle(
        face: Bitmap?,
        hair: Bitmap?,
        shoes: Bitmap?,
        clothes: Bitmap?,
        occasion: String,
        location: String?
    ): FashionAdvice {
        val downscaledFace = downscale(face)
        val faceHex = extractFaceTelemetryHex(downscaledFace)
        val undertone = faceHex?.let { ColorQuantizer.determineTemperature(it) }

        // P1.4.3 Wire ItaCalculator on-device skin telemetry
        val faceIta = downscaledFace?.let { itaCalculator.calculateIta(it) }

        val textPrompt = """
            You are a professional personal color analyst and style consultant. 
            The user is preparing for a specific occasion: $occasion.
            ${if (location != null) "Current Location Context: $location. Take local climate and fashion trends for this area into account." else ""}
            
            GOAL:
            Analyze physical attributes and coordinate them with the outfit to recommend a makeup and nail polish palette TAILORED for occasion: $occasion.
            
            TASKS:
            1. Identify Seasonal Type (SPRING, SUMMER, AUTUMN, WINTER) of the face.
            2. Identify Undertone (WARM, COOL, NEUTRAL) of the face.
            3. Recommend makeup and nail polish palette.
            
            Respond ONLY with valid JSON matching schema:
            {
              "summary": "string",
              "seasonalType": "SPRING",
              "undertone": "WARM",
              "makeupSuggestions": [],
              "outfitSuggestions": [],
              "recommendedPalette": ["#HEX"]
            }
        """.trimIndent()

        val finalPrompt = if (faceIta != null) {
            "FACE TELEMETRY (L*: ${String.format("%.1f", faceIta.lStar)}, b*: ${String.format("%.1f", faceIta.bStar)}, ITA°: ${String.format("%.1f", faceIta.itaScore)}, Skin Tone: ${faceIta.skinToneCategory}, Undertone: ${undertone?.name ?: "NEUTRAL"})\n$textPrompt"
        } else {
            textPrompt
        }

        return try {
            val provider = capabilityRouter.getRankedAvailableProviders().firstOrNull()
                ?: throw IllegalStateException("No AI providers available")

            val input = if (provider.capability.supportsLocalImageIngestion && downscaledFace != null) {
                AiInput.Multimodal(
                    promptString = finalPrompt,
                    localImage = downscaledFace
                )
            } else {
                AiInput.TextOnly(promptString = finalPrompt)
            }

            val response = provider.execute(input)
            val jsonText = response.getOrNull() ?: throw IllegalStateException("Empty response from AI")
            
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
        image: Bitmap
    ): CosmeticItem? {
        val downscaledImage = downscale(image) ?: return null
        val promptText = """
            Identify this cosmetic product from the image. 
            Extract product name, brand, shade name, and hex color.
            Categorize into: ${MicroCategory.entries.filter { it != MicroCategory.AI_PENDING }.joinToString(", ") { it.name }}
            
            Respond ONLY with valid JSON schema:
            {
              "name": "string",
              "brand": "string",
              "microCategory": "LIPSTICK",
              "shadeName": "string",
              "colorHex": "#HEX",
              "notes": "string"
            }
        """.trimIndent()

        return try {
            val provider = capabilityRouter.getRankedAvailableProviders().firstOrNull() ?: return null
            val input = if (provider.capability.supportsLocalImageIngestion) {
                AiInput.Multimodal(promptString = promptText, localImage = downscaledImage)
            } else {
                AiInput.TextOnly(promptString = promptText)
            }

            val response = provider.execute(input)
            val jsonText = response.getOrNull() ?: return null
            val cleanedJson = jsonText.substringAfter("{").substringBeforeLast("}")
            val finalJson = "{$cleanedJson}"
            
            val result = json.decodeFromString<CosmeticItemJson>(finalJson)
            val micro = try { MicroCategory.valueOf(result.microCategory) } catch (e: Exception) { MicroCategory.OTHER }
            
            CosmeticItem(
                name = result.name,
                brand = result.brand,
                macroCategory = micro.macro,
                microCategory = micro,
                colorHex = result.colorHex ?: "#FFFFFF",
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
    val microCategory: String,
    val shadeName: String? = null,
    val colorHex: String? = null,
    val notes: String? = null
)
