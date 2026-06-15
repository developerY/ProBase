package com.zoewave.probase.kocolor.features.analyzer.data

import com.google.ai.client.generativeai.GenerativeModel
import com.zoewave.probase.core.data.repository.AiConfigurationSettings
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class IngredientRisk(
    val name: String,
    val type: RiskType,
    val severity: Int, // 1 to 10
    val explanation: String
)

enum class RiskType {
    ALLERGEN, IRRITANT, PREGNANCY_CAUTION, ENDOCRINE_DISRUPTOR, CARCINOGEN
}

@Serializable
data class IngredientAnalysis(
    val ingredients: List<String>,
    val risks: List<IngredientRisk>,
    val overallSafetyScore: Int,
    val recommendations: List<String>
)

/**
 * AI-powered service for decoding and analyzing cosmetic ingredient lists.
 */
@Singleton
class IngredientIntelligence @Inject constructor(
    private val aiSettings: AiConfigurationSettings
) {

    private suspend fun getGeminiModel(): GenerativeModel? {
        val apiKey = aiSettings.getGeminiApiKey() ?: return null
        return GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = apiKey
        )
    }

    /**
     * Analyzes an ingredient list string (INCI) and returns structured analysis.
     */
    suspend fun analyzeIngredients(inciText: String): IngredientAnalysis? {
        val model = getGeminiModel() ?: return null
        
        val prompt = """
            Analyze the following cosmetic ingredient list (INCI):
            "${inciText}"
            
            Provide a JSON response with:
            1. 'ingredients': List of parsed ingredient names.
            2. 'risks': List of objects with 'name', 'type' (ALLERGEN, IRRITANT, PREGNANCY_CAUTION, ENDOCRINE_DISRUPTOR, CARCINOGEN), 'severity' (1-10), and 'explanation'.
            3. 'overallSafetyScore': An integer from 1 to 10 (10 being safest).
            4. 'recommendations': Actionable advice for the user.
            
            Return ONLY the JSON.
        """.trimIndent()

        return try {
            val response = model.generateContent(prompt)
            val jsonText = response.text?.replace("```json", "")?.replace("```", "")?.trim()
            if (jsonText != null) {
                kotlinx.serialization.json.Json.decodeFromString<IngredientAnalysis>(jsonText)
            } else null
        } catch (e: Exception) {
            null
        }
    }
}
