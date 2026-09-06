package com.zoewave.probase.kocolor.data.usecase

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IntentAnalyzer @Inject constructor() {

    fun analyze(intentString: String): StyleIntentProfile {
        val keywords = intentString.lowercase().split(Regex("[\\s,.]+")).filter { it.isNotBlank() }
        
        var colorfulness = 0.5f
        var novelty = 0.5f
        var formality = 0.5f
        var colorContrast = 0.5f

        for (word in keywords) {
            when (word) {
                "colorful" -> colorfulness += 0.8f
                "vibrant" -> colorfulness += 0.9f
                "bright" -> colorfulness += 0.7f
                "neon" -> { colorfulness += 0.9f; novelty += 0.4f }
                "fun" -> { colorfulness += 0.4f; novelty += 0.5f }
                "minimalist", "minimal" -> { colorfulness -= 0.7f; novelty -= 0.6f; colorContrast -= 0.4f }
                "muted", "subtle" -> { colorfulness -= 0.6f; colorContrast -= 0.5f }
                "professional", "work", "office" -> { formality += 0.8f; novelty -= 0.3f; colorfulness -= 0.3f }
                "casual", "weekend", "relaxed" -> { formality -= 0.8f }
                "bold", "statement" -> { novelty += 0.7f; colorContrast += 0.6f }
                "elegant", "sophisticated" -> { formality += 0.5f; colorContrast += 0.2f }
                "monochrome", "monochromatic" -> { colorfulness -= 0.5f; colorContrast -= 0.8f }
                "contrast", "contrasting" -> { colorContrast += 0.8f }
            }
        }

        return StyleIntentProfile(
            colorfulness = colorfulness.coerceIn(0.0f, 1.0f),
            colorContrast = colorContrast.coerceIn(0.0f, 1.0f),
            novelty = novelty.coerceIn(0.0f, 1.0f),
            formality = formality.coerceIn(0.0f, 1.0f)
        )
    }
}
