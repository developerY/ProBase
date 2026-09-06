package com.zoewave.probase.kocolor.data.usecase

import android.graphics.Color
import androidx.core.graphics.ColorUtils
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.core.model.ritual.CosmeticItem
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt
import kotlin.math.pow
import kotlin.math.abs

@Singleton
class IntentFulfillmentEvaluator @Inject constructor() {

    fun evaluate(
        intentProfile: StyleIntentProfile,
        selectedClothing: List<ClothingItem>,
        selectedCosmetics: List<CosmeticItem>
    ): IntentFulfillment {
        val clothingChromaValues = selectedClothing.mapNotNull { calculateChroma(it.colorHex) }
        val cosmeticChromaValues = selectedCosmetics.mapNotNull { calculateChroma(it.colorHex) }
        val allChromaValues = clothingChromaValues + cosmeticChromaValues

        val maxChroma = allChromaValues.maxOrNull() ?: 0.0f
        val meanChroma = if (allChromaValues.isNotEmpty()) allChromaValues.average().toFloat() else 0.0f
        
        val chromaticItemCount = allChromaValues.count { it > 30.0f }
        val chromaticRatio = if (allChromaValues.isNotEmpty()) chromaticItemCount.toFloat() / allChromaValues.size else 0.0f

        val evaluatedColorfulness = ((maxChroma / 120.0f) * 0.4f) + (meanChroma / 100.0f * 0.4f) + (chromaticRatio * 0.2f)
        val normalizedEvaluatedColorfulness = evaluatedColorfulness.coerceIn(0.0f, 1.0f)

        var colorfulnessDelta = 1.0f - abs(intentProfile.colorfulness - normalizedEvaluatedColorfulness)
        
        // Asymmetric penalty: If they want colorful and we gave them neutral, hit them hard.
        if (intentProfile.colorfulness > 0.7f && normalizedEvaluatedColorfulness < 0.3f) {
            colorfulnessDelta *= 0.5f 
        }

        // Just use colorfulness as the main score for now
        val score = (colorfulnessDelta * 100.0f).coerceIn(0.0f, 100.0f)

        val dimensions = IntentFulfillmentDimensions(
            colorfulness = normalizedEvaluatedColorfulness,
            colorContrast = 0.5f,
            novelty = 0.5f,
            formality = 0.5f
        )

        val unmet = mutableListOf<String>()
        if (intentProfile.colorfulness > 0.6f && dimensions.colorfulness < 0.6f) {
            unmet.add("Colorfulness")
        }
        if (intentProfile.colorContrast > 0.6f && dimensions.colorContrast < 0.6f) {
            unmet.add("Color Contrast")
        }
        if (intentProfile.novelty > 0.6f && dimensions.novelty < 0.6f) {
            unmet.add("Novelty")
        }
        if (score < 70.0f && unmet.isEmpty()) {
            unmet.add("Colorfulness & Vibrancy")
        }

        return IntentFulfillment(
            score = score,
            dimensions = dimensions,
            unmetIntent = unmet
        )
    }

    private fun calculateChroma(hex: String?): Float? {
        if (hex == null) return null
        return try {
            val colorInt = Color.parseColor(hex)
            val lab = DoubleArray(3)
            ColorUtils.colorToLAB(colorInt, lab)
            val a = lab[1]
            val b = lab[2]
            sqrt(a.pow(2.0) + b.pow(2.0)).toFloat()
        } catch (e: Exception) {
            null
        }
    }
}
