package com.zoewave.probase.features.ai.local.domain.router

import android.graphics.Bitmap

/**
 * Exception thrown when the local AI pipeline cannot fulfill a request
 * and requires delegation to a Cloud Gemini (BYOK) model.
 */
class RequiresCloudException(reason: String) : Exception(reason)

/**
 * Encapsulates the dynamic routing logic for on-device Gemini Nano prompts.
 */
object GeminiPipelineRouter {

    private const val LATIN_THRESHOLD = 0.8 // 80% Latin characters required

    /**
     * Checks if the text is primarily standard Latin characters and INCI nomenclature.
     */
    fun isValidLatinScript(ocrText: String): Boolean {
        if (ocrText.isBlank()) return true
        
        val latinChars = ocrText.count { it in 'a'..'z' || it in 'A'..'Z' || it.isDigit() || it.isWhitespace() || it in ".,()-%" }
        val totalChars = ocrText.length
        
        return (latinChars.toDouble() / totalChars.toDouble()) >= LATIN_THRESHOLD
    }

    /**
     * Constructs the correct prompt and input configuration based on hardware and script.
     */
    fun route(
        ocrText: String,
        bitmap: Bitmap?,
        isMultimodalSupported: Boolean,
        isAicoreAvailable: Boolean
    ): PipelineConfig {
        if (!isAicoreAvailable) {
            throw RequiresCloudException("AICore is not available on this device")
        }

        val isLatin = isValidLatinScript(ocrText)

        return when {
            // PATH C: Flagship Device + Non-Latin Script (Pure Vision)
            isMultimodalSupported && !isLatin -> {
                PipelineConfig(
                    prompt = "You are an expert cosmetic chemist. I am providing you with an image of a foreign cosmetic product. Read the text directly from the image. Identify the canonical Brand Name, Product Name, Active Ingredients, and Inactive Ingredients. Translate all extracted data into English. Return ONLY a valid JSON object matching the requested schema.",
                    inputImage = bitmap,
                    inputOcrText = null, // Drop OCR to prevent hallucination
                    path = "PATH_C_PURE_VISION"
                )
            }
            // PATH B: Flagship Device + Latin Script (OCR-Anchored Multimodal)
            isMultimodalSupported && isLatin -> {
                PipelineConfig(
                    prompt = "You are an expert cosmetic chemist. I am providing you with an image of a cosmetic product AND the raw OCR text extracted from it. Use the image to understand the visual layout and context. Use the OCR text as a high-fidelity reference to ensure the exact spelling of complex chemical names. Extract the canonical Brand Name, Product Name, Active Ingredients, and Inactive Ingredients. Translate to English if necessary. Return ONLY a valid JSON object matching the requested schema.",
                    inputImage = bitmap,
                    inputOcrText = ocrText,
                    path = "PATH_B_ANCHORED_MULTIMODAL"
                )
            }
            // PATH A: Standard Device (Text-Only)
            else -> {
                PipelineConfig(
                    prompt = "You are an expert cosmetic chemist. I am providing you with the raw OCR text extracted from a cosmetic product. Read the text, fix any OCR spelling errors, and extract the canonical Brand Name, Product Name, Active Ingredients, and Inactive Ingredients. Translate any foreign warnings to English. Return ONLY a valid JSON object matching the requested schema.",
                    inputImage = null,
                    inputOcrText = ocrText,
                    path = "PATH_A_TEXT_ONLY"
                )
            }
        }
    }

    data class PipelineConfig(
        val prompt: String,
        val inputImage: Bitmap?,
        val inputOcrText: String?,
        val path: String,
        val jsonSchema: String = """
            {
              "brand_name": "String",
              "product_name": "String",
              "volume": "String (e.g., 3.0 FL OZ)",
              "active_ingredients": ["String"],
              "inactive_ingredients": ["String"]
            }
        """.trimIndent()
    )
}
