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
        selectedCosmetics: List<CosmeticItem>,
        rotationScores: Map<String, Double>
    ): IntentFulfillment {
        if (selectedClothing.isEmpty()) {
            return IntentFulfillment(
                state = if (intentProfile.activeDimensions.isEmpty()) StyleIntentState.NotSpecified else StyleIntentState.Specified(intentProfile),
                score = if (intentProfile.activeDimensions.isEmpty()) null else 0f,
                observedMetrics = ObservedEnsembleMetrics(0f, 0f, 0f, 0f),
                unmetIntent = intentProfile.activeDimensions.map { it.name }
            )
        }

        val clothingChromaValues = selectedClothing.mapNotNull { calculateChroma(it.colorHex) }
        val cosmeticChromaValues = selectedCosmetics.mapNotNull { calculateChroma(it.colorHex) }
        val allChromaValues = clothingChromaValues + cosmeticChromaValues

        val maxChroma = allChromaValues.maxOrNull() ?: 0.0f
        val meanChroma = if (allChromaValues.isNotEmpty()) allChromaValues.average().toFloat() else 0.0f
        
        val chromaticItemCount = allChromaValues.count { it > 30.0f }
        val chromaticRatio = if (allChromaValues.isNotEmpty()) chromaticItemCount.toFloat() / allChromaValues.size else 0.0f

        val evaluatedColorfulness = ((maxChroma / 120.0f) * 0.4f) + (meanChroma / 100.0f * 0.4f) + (chromaticRatio * 0.2f)
        val normalizedEvaluatedColorfulness = evaluatedColorfulness.coerceIn(0.0f, 1.0f)

        val clothingLStars = selectedClothing.mapNotNull { calculateLStar(it.colorHex) }
        val cosmeticLStars = selectedCosmetics.mapNotNull { calculateLStar(it.colorHex) }
        val allLStars = clothingLStars + cosmeticLStars
        
        val colorContrast = if (allLStars.size >= 2) {
            ((allLStars.maxOrNull()!! - allLStars.minOrNull()!!) / 85.0f).coerceIn(0.0f, 1.0f)
        } else {
            0.0f
        }

        val novelty = if (selectedClothing.isNotEmpty()) {
            selectedClothing.map { 1.0f - (rotationScores["w_${it.internalId}"]?.toFloat() ?: 0.0f) }.average().toFloat()
        } else {
            1.0f
        }

        val formality = if (selectedClothing.isNotEmpty()) {
            selectedClothing.map { (it.formality.rank - 1) / 5f }.average().toFloat()
        } else {
            0.5f
        }

        val observedMetrics = ObservedEnsembleMetrics(
            colorfulness = normalizedEvaluatedColorfulness,
            colorContrast = colorContrast,
            novelty = novelty,
            formality = formality
        )

        val effectiveActiveDimensions = intentProfile.activeDimensions.ifEmpty {
            buildSet {
                if (kotlin.math.abs(intentProfile.colorfulness - 0.5f) > 0.01f) add(IntentDimension.COLORFULNESS)
                if (kotlin.math.abs(intentProfile.colorContrast - 0.5f) > 0.01f) add(IntentDimension.CONTRAST)
                if (kotlin.math.abs(intentProfile.novelty - 0.5f) > 0.01f) add(IntentDimension.NOVELTY)
                if (kotlin.math.abs(intentProfile.formality - 0.5f) > 0.01f) add(IntentDimension.FORMALITY)
            }
        }

        if (effectiveActiveDimensions.isEmpty()) {
            return IntentFulfillment(
                state = StyleIntentState.NotSpecified,
                score = null,
                observedMetrics = observedMetrics,
                unmetIntent = emptyList()
            )
        }

        var totalError = 0.0f
        val unmet = mutableListOf<String>()

        if (IntentDimension.COLORFULNESS in effectiveActiveDimensions) {
            var error = abs(intentProfile.colorfulness - observedMetrics.colorfulness)
            if (intentProfile.colorfulness > 0.7f && observedMetrics.colorfulness < 0.3f) {
                error += 0.5f // Asymmetric penalty
            }
            totalError += error
            if (intentProfile.colorfulness > 0.6f && observedMetrics.colorfulness < 0.6f) unmet.add("Colorfulness")
        }
        
        if (IntentDimension.CONTRAST in effectiveActiveDimensions) {
            val error = abs(intentProfile.colorContrast - observedMetrics.colorContrast)
            totalError += error
            if (intentProfile.colorContrast > 0.6f && observedMetrics.colorContrast < 0.6f) unmet.add("Color Contrast")
        }
        
        if (IntentDimension.NOVELTY in effectiveActiveDimensions) {
            val error = abs(intentProfile.novelty - observedMetrics.novelty)
            totalError += error
            if (intentProfile.novelty > 0.6f && observedMetrics.novelty < 0.6f) unmet.add("Novelty")
        }
        
        if (IntentDimension.FORMALITY in effectiveActiveDimensions) {
            val error = abs(intentProfile.formality - observedMetrics.formality)
            totalError += error
        }

        val avgError = totalError / effectiveActiveDimensions.size
        val score = ((1.0f - avgError) * 100.0f).coerceIn(0.0f, 100.0f)
        
        if (score < 70.0f && unmet.isEmpty()) {
            unmet.add("General Alignment")
        }

        return IntentFulfillment(
            state = StyleIntentState.Specified(intentProfile),
            score = score,
            observedMetrics = observedMetrics,
            unmetIntent = unmet
        )
    }

    private fun calculateLStar(hex: String?): Float? {
        if (hex == null) return null
        return try {
            val colorInt = Color.parseColor(hex)
            val lab = DoubleArray(3)
            ColorUtils.colorToLAB(colorInt, lab)
            lab[0].toFloat()
        } catch (e: Exception) {
            null
        }
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
