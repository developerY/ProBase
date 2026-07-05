package com.zoewave.probase.features.ai.local

import android.content.Context
import android.util.Log
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
        Log.d("AI_SANDBOX", "Nano: Starting cleanOcrText. Input length: ${rawOcrText.length}")
        try {
            // 1. Prepare engine (Check if hardware supports it)
            Log.d("AI_SANDBOX", "Nano: Calling prepareInferenceEngine()...")
            try {
                model.prepareInferenceEngine()
                Log.d("AI_SANDBOX", "Nano: prepareInferenceEngine() SUCCESS. Silicon is ready.")
            } catch (e: Exception) {
                Log.e("AI_SANDBOX", "Nano: prepareInferenceEngine() FAILED: ${e.message}", e)
                return@withContext "NANO_UNSUPPORTED: ${e.message}"
            }

            // 2. Prepare the prompt
            val prompt = """
                You are a data cleaner. Fix any OCR spelling errors in the following text. 
                Extract the Brand Name and Product Name.
                
                Raw OCR Text:
                $rawOcrText
            """.trimIndent()
            
            Log.d("AI_SANDBOX", "Nano: Sending Prompt to Silicon:\n$prompt")

            // 3. Run local inference
            Log.d("AI_SANDBOX", "Nano: Executing generateContent...")
            val response = model.generateContent(prompt)
            val result = response.text ?: "NANO_RETURNED_NULL"
            
            Log.d("AI_SANDBOX", "Nano: generateContent FINISHED. Response: $result")
            return@withContext result

        } catch (e: Exception) {
            Log.e("AI_SANDBOX", "Nano: CRASHED during inference: ${e.message}", e)
            return@withContext "NANO_CRASHED: ${e.message}"
        }
    }
}
