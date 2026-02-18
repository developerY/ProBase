package com.zoewave.probase.features.nav3.ui.inventory


import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

// 1. Mark the sealed interface as NavKey
sealed interface FeatureInventory : NavKey {
    @Serializable
    data object List : FeatureInventory

    @Serializable
    data object Health : FeatureInventory

    @Serializable
    data object Weather : FeatureInventory
    @Serializable
    data object Ble : FeatureInventory
    @Serializable
    data object Nfc : FeatureInventory

    @Serializable
    data object QrScanner : FeatureInventory
}