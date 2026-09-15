package com.zoewave.probase.kocolor.features.store.ui

sealed interface StoreEvent {
    data object ToggleExpansion : StoreEvent
    data object EnterStore : StoreEvent
    data class AddProductToInventory(val itemId: String) : StoreEvent
    data class ToggleFavorite(val itemId: String) : StoreEvent
}
