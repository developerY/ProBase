package com.zoewave.probase.features.ai.vision.receipt.data

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.zoewave.probase.features.ai.vision.receipt.ReceiptDiagnosticResult
import com.zoewave.probase.features.ai.vision.receipt.ReceiptEngine
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject

@Serializable
private data class GeminiReceiptDraft(
    val merchant: String? = null,
    val total: Double? = null,
    val date: String? = null,
    val category: String? = null,
    val importance: String? = null,
    val whatIsThis: String? = null,
    val financialImpact: String? = null
)

class CloudReceiptEngine @Inject constructor() : ReceiptEngine {

    private val json = Json { 
        ignoreUnknownKeys = true 
        coerceInputValues = true
    }

    override suspend fun processReceipt(
        bitmap: Bitmap,
        apiKey: String?,
        modelName: String?,
        userContext: String?
    ): ReceiptDiagnosticResult {
        val logs = mutableListOf("Cloud Receipt AI initialized")
        if (apiKey.isNullOrBlank()) {
            throw IllegalArgumentException("Missing Gemini API Key")
        }

        val finalModelName = modelName ?: "gemini-1.5-flash"
        logs.add("Model: $finalModelName")

        val generativeModel = GenerativeModel(
            modelName = finalModelName,
            apiKey = apiKey,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
            }
        )

        val prompt = content {
            image(bitmap)
            text("""
                what is this a picture of?
                ${if (!userContext.isNullOrBlank()) "Context: '$userContext'" else ""}
                
                Fields:
                - merchant: The name of the store or service provider.
                - total: The final amount paid. Return strictly as a number (e.g. 15.50).
                - date: The transaction date. Convert to MM/DD/YYYY format.
                - category: Suggest a broad category (e.g. Food, Travel, Office, Shopping).
                - importance: Decide if this purchase is a NEED (essential) or a WANT (discretionary).
                - whatIsThis: A detailed description of what this is a picture of.
                - financialImpact: Describe how this specific purchase affects the user's finances based on the provided context if available. Be concise.
                
                Respond ONLY with valid JSON matching this schema:
                {
                  "merchant": string or null,
                  "total": number or null,
                  "date": string or null,
                  "category": string or null,
                  "importance": "NEED" | "WANT" | null,
                  "whatIsThis": string or null,
                  "financialImpact": string or null
                }
            """.trimIndent())
        }

        return try {
            logs.add("Sending multimodal request to Gemini...")
            val response = generativeModel.generateContent(prompt)
            val jsonText = response.text ?: throw IllegalStateException("Empty response")
            
            val draft = json.decodeFromString<GeminiReceiptDraft>(jsonText)
            logs.add("JSON decoding successful")
            
            ReceiptDiagnosticResult(
                merchant = draft.merchant,
                total = draft.total,
                date = draft.date,
                category = draft.category,
                importance = draft.importance,
                logs = logs,
                engineUsed = "Cloud AI (Gemini)",
                rawResponse = jsonText,
                whatIsThis = draft.whatIsThis,
                financialImpact = draft.financialImpact
            )
        } catch (e: Exception) {
            logs.add("Cloud API failed: ${e.localizedMessage}")
            throw e
        }
    }
}
