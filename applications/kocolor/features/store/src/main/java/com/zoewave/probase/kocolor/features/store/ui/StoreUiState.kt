package com.zoewave.probase.kocolor.features.store.ui

import androidx.compose.ui.graphics.Color
import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.PackInfo

data class StoreProductItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: String,
    val price: String,
    val numericPrice: Double = 48.0,
    val detailTag: String,
    val badge: String,
    val shadeName: String? = null,
    val shadeColor: Color? = null,
    val imageModel: Any? = null,
    val isAdded: Boolean = false,
    val isFavorite: Boolean = false
)

data class StoreUiState(
    val cosmeticsPacks: List<PackInfo> = emptyList(),
    val fashionPacks: List<PackInfo> = emptyList(),
    val realCosmeticItems: List<StoreProductItem> = emptyList(),
    val realFashionItems: List<StoreProductItem> = emptyList(),
    val cartItems: List<StoreProductItem> = emptyList(),
    val favoriteItemIds: Set<String> = emptySet(),
    val isCartOpen: Boolean = false,
    val isCheckoutSuccess: Boolean = false,
    val isExpanded: Boolean = false,
    val backgroundModel: Any? = null,
    val categoryProgress: List<CategoryProgressItem> = emptyList()
) {
    val cartCount: Int get() = cartItems.size
    val cartTotalPrice: Double get() = cartItems.sumOf { it.numericPrice }
}
