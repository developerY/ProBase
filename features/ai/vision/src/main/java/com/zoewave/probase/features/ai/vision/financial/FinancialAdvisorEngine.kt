package com.zoewave.probase.features.ai.vision.financial

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.zoewave.probase.features.readers.ocr.data.LocalOcrEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinancialAdvisorEngine @Inject constructor(
    private val ocrEngine: LocalOcrEngine
) {

    suspend fun analyzeFinancialImpact(
        bitmap: Bitmap,
        apiKey: String,
        modelName: String,
        financialContext: String? = null,
        userContext: String? = null,
        deviceBranding: String = "phone"
    ): String = withContext(Dispatchers.Default) {
        val model = GenerativeModel(
            modelName = modelName,
            apiKey = apiKey
        )

        val visionText = ocrEngine.extractText(bitmap)

        val prompt = """
            You are a helpful financial assistant on a $deviceBranding.
            Look at this image. The user is asking if they can afford this or what the impact is.
            
            Current Financial Context:
            ${financialContext ?: "No context available."}
            
            ${userContext?.let { "User Input: $it" } ?: ""}
            Extracted text from image: $visionText
            
            Task:
            1. Identify the product/item and its price if possible.
            2. Tell the user if they can afford it based on their "Flexible Money Remaining".
            3. Provide a concise, spoken-style advice (max 2 sentences).
        """.trimIndent()

        val inputContent = content {
            image(bitmap)
            text(prompt)
        }

        try {
            val response = model.generateContent(inputContent)
            response.text ?: "I couldn't analyze the image."
        } catch (e: Exception) {
            "Error: ${e.localizedMessage}"
        }
    }
}
