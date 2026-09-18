package com.zoewave.probase.kocolor.mobile.features.home.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.zoewave.probase.core.model.ritual.SavedAnalysis

data class CategoryProgressItem(
    val label: String,
    val owned: Int,
    val target: Int,
    val color: Color
)

data class ArchiveVerticalUiState(
    val title: String,
    val count: Int,
    val countLabel: String,
    val valueLabel: String,
    val value: Double,
    val imageModel: Any,
    val icon: ImageVector,
    val discoverTitle: String? = null,
    val discoverSubtitle: String? = null,
    val discoverColor: Color = Color(0xFF3D223B),
    val onDiscoverClick: (() -> Unit)? = null,
    val categoryProgress: List<CategoryProgressItem> = emptyList(),
    val healthMetric: String? = null,
    val restockMetric: String? = null,
    val chromaticTone: String? = null,
    val avgCpu: Double? = null,
    val breakdown: Map<String, Int> = emptyMap()
)

data class CuratedCollectionUiState(val analysis: SavedAnalysis)
