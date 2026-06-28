package com.zoewave.probase.kocolor.features.store.ui

sealed interface StoreEvent {
    data object ToggleExpansion : StoreEvent
    data object EnterStore : StoreEvent
}
