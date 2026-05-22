package com.zoewave.probase.seaweed.mobile.glass

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SeaweedGlassVisionEngine @Inject constructor() {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun analyzeImage(
        bitmap: Bitmap,
        apiKey: String,
        modelName: String,
        financialContext: String? = null,
        userContext: String? = null
    ): String = withContext(Dispatchers.Default) {
        val model = GenerativeModel(
            modelName = modelName,
            apiKey = apiKey
        )

        val image = InputImage.fromBitmap(bitmap, 0)
        val visionText = try {
            recognizer.process(image).await().text
        } catch (_: Exception) {
            ""
        }

        val prompt = """
            You are a helpful financial assistant on a pair of smart glasses.
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
