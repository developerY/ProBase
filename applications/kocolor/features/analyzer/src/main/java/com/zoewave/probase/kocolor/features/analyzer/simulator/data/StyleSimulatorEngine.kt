package com.zoewave.probase.kocolor.features.analyzer.simulator.data

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.zoewave.probase.kocolor.model.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StyleSimulatorEngine @Inject constructor() {

    private val json = Json { 
        ignoreUnknownKeys = true 
        coerceInputValues = true
    }

    suspend fun architectStyleBlueprint(
        userIntent: String,
        circadianContext: String,
        routineCompleted: Boolean,
        wellnessScore: Double,
        availableWardrobe: List<ClothingItem>,
        apiKey: String,
        modelName: String = "gemini-1.5-flash"
    ): StyleBlueprint {
        val generativeModel = GenerativeModel(
            modelName = modelName,
            apiKey = apiKey,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
            }
        )

        val wardrobeDescription = availableWardrobe.joinToString("\n") { 
            "ID: ${it.id}, Name: ${it.name}, Category: ${it.category}, Color: ${it.colorHex ?: "Unknown"}"
        }

        val prompt = content {
            text("""
                You are the KoColor Style Architect AI. Your task is to generate a "Style Blueprint" for a user based on their intent, biological context, and available wardrobe.
                
                USER INTENT: $userIntent
                CIRCADIAN CONTEXT: $circadianContext
                MORNING RITUAL COMPLETED: $routineCompleted
                WELLNESS SCORE: ${"%.2f".format(wellnessScore)}
                
                AVAILABLE WARDROBE:
                $wardrobeDescription
                
                GOAL:
                1. Select the BEST 3 items from the wardrobe (Top, Bottom, Shoes) that match the intent and context.
                2. Recommend 2 Accessories from the available items.
                3. Create a 3-color Palette (HEX codes) that harmonizes the selection.
                4. Provide a brief stylistic rationale.
                
                Respond ONLY with a valid JSON object matching this exact schema:
                {
                  "rationale": "string",
                  "selectedItemIds": ["Long", "Long", ...],
                  "recommendedPalette": ["#HEX", "#HEX", "#HEX"]
                }
            """.trimIndent())
        }

        return try {
            val response = generativeModel.generateContent(prompt)
            val jsonText = response.text ?: throw IllegalStateException("Empty response from AI")
            val cleanedJson = jsonText.substringAfter("{").substringBeforeLast("}")
            val finalJson = "{$cleanedJson}"
            
            json.decodeFromString<StyleBlueprint>(finalJson)
        } catch (e: Exception) {
            StyleBlueprint(
                rationale = "Blueprint architecture failed: ${e.localizedMessage}",
                selectedItemIds = emptyList(),
                recommendedPalette = listOf("#CCCCCC", "#AAAAAA", "#888888")
            )
        }
    }
}

@Serializable
data class StyleBlueprint(
    val rationale: String,
    val selectedItemIds: List<Long>,
    val recommendedPalette: List<String>
)
