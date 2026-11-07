package com.zoewave.probase.kocolor.fashionista.domain

import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.kocolor.data.usecase.StyleBlueprint
import com.zoewave.probase.kocolor.data.usecase.StyleRequestContext

interface FashionistaEvaluator {
    fun evaluate(
        blueprint: StyleBlueprint,
        userContext: StyleRequestContext,
        resolvedClothing: List<ClothingItem> = emptyList()
    ): FashionistaScore
}
