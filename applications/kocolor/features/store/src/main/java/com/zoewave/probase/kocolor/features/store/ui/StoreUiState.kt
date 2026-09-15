package com.zoewave.probase.kocolor.features.store.ui

import androidx.compose.ui.graphics.Color
import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.PackInfo

data class StoreProductItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: String,
    val price: String,
    val detailTag: String,
    val badge: String,
    val shadeName: String? = null,
    val shadeColor: Color? = null,
    val imageModel: Any? = null
)

data class StoreUiState(
    val cosmeticsPacks: List<PackInfo> = emptyList(),
    val fashionPacks: List<PackInfo> = emptyList(),
    val realCosmeticItems: List<StoreProductItem> = emptyList(),
    val realFashionItems: List<StoreProductItem> = emptyList(),
    val isExpanded: Boolean = false,
    val backgroundModel: Any? = null
)
