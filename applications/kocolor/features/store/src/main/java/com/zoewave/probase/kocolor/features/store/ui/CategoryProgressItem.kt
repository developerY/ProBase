package com.zoewave.probase.kocolor.features.store.ui

import androidx.compose.ui.graphics.Color

data class CategoryProgressItem(
    val label: String,
    val owned: Int,
    val target: Int,
    val color: Color
)
