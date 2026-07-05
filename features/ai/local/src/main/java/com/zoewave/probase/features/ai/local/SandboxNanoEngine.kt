package com.zoewave.probase.features.ai.local

import android.content.Context
import com.google.ai.edge.aicore.GenerativeModel
import com.google.ai.edge.aicore.GenerationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SandboxNanoEngine(private val context: Context) {

    // Using the raw Gemini Nano text model via aicore exp02
    private val model = GenerativeModel(
        generationConfig = GenerationConfig.builder().apply {
            this.context = this@SandboxNanoEngine.context
            this.temperature = 0.1f
        }.build()
    )

    suspend fun cleanOcrText(rawOcrText: String): String = withContext(Dispatchers.IO) {
        try {
            // 1. Prepare engine (Check if hardware supports it)
            try {
                model.prepareInferenceEngine()
            } catch (e: Exception) {
                return@withContext "NANO_UNSUPPORTED: ${e.message}"
            }

            // 2. Prepare the prompt
            val prompt = """
                You are a data cleaner. Fix any OCR spelling errors in the following text. 
                Extract the Brand Name and Product Name.
                
                Raw OCR Text:
                $rawOcrText
            """.trimIndent()

            // 3. Run local inference
            val response = model.generateContent(prompt)
            return@withContext response.text ?: "NANO_RETURNED_NULL"

        } catch (e: Exception) {
            return@withContext "NANO_CRASHED: ${e.message}"
        }
    }
}
