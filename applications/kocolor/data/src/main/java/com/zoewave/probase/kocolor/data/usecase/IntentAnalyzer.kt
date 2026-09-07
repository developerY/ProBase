package com.zoewave.probase.kocolor.data.usecase

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IntentAnalyzer @Inject constructor() {

    fun analyzeState(intentString: String): StyleIntentState {
        val trimmed = intentString.trim()
        if (trimmed.isBlank() || trimmed.equals("Daily Outfit", ignoreCase = true) || trimmed.equals("Daily", ignoreCase = true)) {
            return StyleIntentState.NotSpecified
        }
        val profile = analyze(intentString)
        return if (profile.isSpecified) StyleIntentState.Specified(profile) else StyleIntentState.NotSpecified
    }

    fun analyze(intentString: String): StyleIntentProfile {
        val trimmed = intentString.trim()
        if (trimmed.isBlank() || trimmed.equals("Daily Outfit", ignoreCase = true) || trimmed.equals("Daily", ignoreCase = true)) {
            return StyleIntentProfile(isSpecified = false)
        }

        val keywords = trimmed.lowercase().split(Regex("[\\s,.]+")).filter { it.isNotBlank() }
        if (keywords.isEmpty()) {
            return StyleIntentProfile(isSpecified = false)
        }

        var colorfulness = 0.5f
        var novelty = 0.5f
        var formality = 0.5f
        var colorContrast = 0.5f

        // Explicit High-Chroma Keyword Matching
        if (keywords.any { it == "colorful" || it == "vibrant" || it == "bright" || it == "neon" }) {
            colorfulness = 1.0f
            colorContrast += 0.4f
        }

        for (word in keywords) {
            when (word) {
                "colorful" -> colorfulness = 1.0f
                "vibrant" -> colorfulness = 1.0f
                "bright" -> colorfulness = 1.0f
                "neon" -> { colorfulness = 1.0f; novelty += 0.4f }
                "fun" -> { colorfulness = maxOf(colorfulness, 0.8f); novelty += 0.5f }
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
            isSpecified = true,
            colorfulness = colorfulness.coerceIn(0.0f, 1.0f),
            colorContrast = colorContrast.coerceIn(0.0f, 1.0f),
            novelty = novelty.coerceIn(0.0f, 1.0f),
            formality = formality.coerceIn(0.0f, 1.0f)
        )
    }
}
