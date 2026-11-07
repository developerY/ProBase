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
        return StyleIntentState.Specified(profile)
    }

    fun analyze(intentString: String): StyleIntentProfile {
        val trimmed = intentString.trim()
        if (trimmed.isBlank() || trimmed.equals("Daily Outfit", ignoreCase = true) || trimmed.equals("Daily", ignoreCase = true)) {
            return StyleIntentProfile()
        }

        val keywords = trimmed.lowercase().split(Regex("[\\s,.]+")).filter { it.isNotBlank() }
        if (keywords.isEmpty()) {
            return StyleIntentProfile()
        }

        var colorfulness = 0.5f
        var novelty = 0.5f
        var formality = 0.5f
        var colorContrast = 0.5f

        val activeDims = mutableSetOf<IntentDimension>()

        // Explicit High-Chroma Keyword Matching
        if (keywords.any { it == "colorful" || it == "vibrant" || it == "bright" || it == "neon" }) {
            colorfulness = 1.0f
            colorContrast += 0.4f
            activeDims.add(IntentDimension.COLORFULNESS)
            activeDims.add(IntentDimension.CONTRAST)
        }

        for (word in keywords) {
            when (word) {
                "colorful" -> { colorfulness = 1.0f; activeDims.add(IntentDimension.COLORFULNESS) }
                "vibrant" -> { colorfulness = 1.0f; activeDims.add(IntentDimension.COLORFULNESS) }
                "bright" -> { colorfulness = 1.0f; activeDims.add(IntentDimension.COLORFULNESS) }
                "neon" -> { colorfulness = 1.0f; novelty += 0.4f; activeDims.addAll(listOf(IntentDimension.COLORFULNESS, IntentDimension.NOVELTY)) }
                "fun" -> { colorfulness = maxOf(colorfulness, 0.8f); novelty += 0.5f; activeDims.addAll(listOf(IntentDimension.COLORFULNESS, IntentDimension.NOVELTY)) }
                "minimalist", "minimal" -> { colorfulness -= 0.7f; novelty -= 0.6f; colorContrast -= 0.4f; activeDims.addAll(listOf(IntentDimension.COLORFULNESS, IntentDimension.NOVELTY, IntentDimension.CONTRAST)) }
                "muted", "subtle" -> { colorfulness -= 0.6f; colorContrast -= 0.5f; activeDims.addAll(listOf(IntentDimension.COLORFULNESS, IntentDimension.CONTRAST)) }
                "professional", "work", "office" -> { formality += 0.8f; novelty -= 0.3f; colorfulness -= 0.3f; activeDims.addAll(listOf(IntentDimension.FORMALITY, IntentDimension.NOVELTY, IntentDimension.COLORFULNESS)) }
                "casual", "weekend", "relaxed" -> { formality -= 0.8f; activeDims.add(IntentDimension.FORMALITY) }
                "bold", "statement" -> { novelty += 0.7f; colorContrast += 0.6f; activeDims.addAll(listOf(IntentDimension.NOVELTY, IntentDimension.CONTRAST)) }
                "elegant", "sophisticated" -> { formality += 0.5f; colorContrast += 0.2f; activeDims.addAll(listOf(IntentDimension.FORMALITY, IntentDimension.CONTRAST)) }
                "monochrome", "monochromatic" -> { colorfulness -= 0.5f; colorContrast -= 0.8f; activeDims.addAll(listOf(IntentDimension.COLORFULNESS, IntentDimension.CONTRAST)) }
                "contrast", "contrasting" -> { colorContrast += 0.8f; activeDims.add(IntentDimension.CONTRAST) }
            }
        }

        return StyleIntentProfile(
            colorfulness = colorfulness.coerceIn(0.0f, 1.0f),
            colorContrast = colorContrast.coerceIn(0.0f, 1.0f),
            novelty = novelty.coerceIn(0.0f, 1.0f),
            formality = formality.coerceIn(0.0f, 1.0f),
            activeDimensions = activeDims
        )
    }
}
