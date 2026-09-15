package com.zoewave.probase.kocolor.features.store.ui

import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.PackInfo

data class StoreUiState(
    val cosmeticsPacks: List<PackInfo> = emptyList(),
    val fashionPacks: List<PackInfo> = emptyList(),
    val isExpanded: Boolean = false, // Keep for backward compatibility with existing component
    val backgroundModel: Any? = null
)
