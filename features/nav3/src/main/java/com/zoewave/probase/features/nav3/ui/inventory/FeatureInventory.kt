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

    @Serializable
    data object BarcodeScanner : FeatureInventory

    @Serializable
    data object Camera : FeatureInventory

    @Serializable
    data object Calendar : FeatureInventory

    @Serializable
    data object SmartCapture : FeatureInventory

    @Serializable
    data object GlassXR : FeatureInventory

    @Serializable
    data object FullXR : FeatureInventory

    @Serializable
    data class SmartAdvice(val projectId: Long) : FeatureInventory
}
