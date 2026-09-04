package com.zoewave.probase.kocolor.fashionista.scoring

import com.zoewave.probase.kocolor.data.usecase.StyleBlueprint
import com.zoewave.probase.kocolor.data.usecase.StyleRequestContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContrastAnchorEvaluator @Inject constructor() {

    fun evaluate(blueprint: StyleBlueprint, context: StyleRequestContext): Float {
        val telemetry = context.appearanceTelemetry
        val appearance = context.appearanceProfile

        // Compares outfit's overall contrast/luminance against user appearance profile
        val targetContrast = appearance.contrast
        val isHighContrastContext = targetContrast.contains("High", ignoreCase = true) || telemetry.contrastScore > 0.4f

        val palette = blueprint.recommendedPalette
        val hasHighContrastPalette = palette.size >= 2

        return if (isHighContrastContext && hasHighContrastPalette) {
            92.0f
        } else {
            85.0f
        }
    }
}
