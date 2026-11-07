package com.zoewave.probase.kocolor.fashionista.scoring

import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.kocolor.data.usecase.StyleBlueprint
import com.zoewave.probase.kocolor.data.usecase.StyleRequestContext
import com.zoewave.probase.kocolor.fashionista.domain.FashionistaEvaluator
import com.zoewave.probase.kocolor.fashionista.domain.FashionistaScore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FashionistaEngine @Inject constructor(
    private val colorHarmonicsEvaluator: ColorHarmonicsEvaluator,
    private val silhouetteGrader: SilhouetteGrader,
    private val contrastAnchorEvaluator: ContrastAnchorEvaluator
) : FashionistaEvaluator {

    override fun evaluate(
        blueprint: StyleBlueprint,
        userContext: StyleRequestContext,
        resolvedClothing: List<ClothingItem>
    ): FashionistaScore {
        val colorScore = colorHarmonicsEvaluator.evaluate(blueprint)
        val silhouetteScore = silhouetteGrader.grade(blueprint, resolvedClothing)
        val contrastScore = contrastAnchorEvaluator.evaluate(blueprint, userContext)

        val totalScore = (colorScore * 0.40f) + (silhouetteScore * 0.35f) + (contrastScore * 0.25f)

        return FashionistaScore(
            colorHarmonyScore = colorScore,
            silhouetteScore = silhouetteScore,
            contrastScore = contrastScore,
            totalScore = totalScore,
            isApproved = totalScore >= 80.0f
        )
    }
}
