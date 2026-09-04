package com.zoewave.probase.kocolor.fashionista.domain

import com.zoewave.probase.kocolor.data.usecase.StyleBlueprint
import com.zoewave.probase.kocolor.data.usecase.StyleRequestContext

interface FashionistaEvaluator {
    fun evaluate(blueprint: StyleBlueprint, userContext: StyleRequestContext): FashionistaScore
}
