package com.zoewave.probase.ui.components

// Feature Routes


import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.zoewave.probase.feature.weather.ui.WeatherUiRoute
import com.zoewave.probase.features.ble.ui.BluetoothLeRoute
import com.zoewave.probase.features.health.ui.HealthRoute
import com.zoewave.probase.features.nav3.ui.inventory.FeatureInventory
import com.zoewave.probase.features.nav3.ui.inventory.FeatureInventoryScreen
import com.zoewave.probase.features.nfc.ui.NfcUiRoute
import com.zoewave.probase.features.qrscanner.ui.QRCodeScannerScreen


fun featureInventoryEntryProvider(
    key: NavKey,
    navigateTo: (NavKey) -> Unit,
    navigateBack: () -> Unit // ✅ Receive the back action
): NavEntry<NavKey> {

    // We wrap the content in a NavEntry, casting the key back to our specific type
    return NavEntry(key) {
        when (key) {
            is FeatureInventory.List -> {
                FeatureInventoryScreen(
                    onNavigateToHealth = { navigateTo(FeatureInventory.Health) },
                    onNavigateToWeather = { navigateTo(FeatureInventory.Weather) },
                    onNavigateToBle = { navigateTo(FeatureInventory.Ble) },
                    onNavigateToNfc = { navigateTo(FeatureInventory.Nfc) },
                    onNavigateToQrScanner = { navigateTo(FeatureInventory.QrScanner) }
                )
            }

            is FeatureInventory.Health -> {
                FeatureScaffold(title = "Health", onBack = navigateBack) {
                    HealthRoute()
                }
            }

            is FeatureInventory.Weather -> {
                FeatureScaffold(title = "Weather", onBack = navigateBack) {
                    WeatherUiRoute()
                }
            }

            is FeatureInventory.Ble -> {
                FeatureScaffold(title = "BLE", onBack = navigateBack) {
                    BluetoothLeRoute(
                        paddingValues = PaddingValues(0.dp),
                    )
                }
            }

            is FeatureInventory.Nfc -> {
                FeatureScaffold(title = "NFC", onBack = navigateBack) {
                    NfcUiRoute()
                }
            }

            is FeatureInventory.QrScanner -> {
                FeatureScaffold(title = "QR Scanner", onBack = navigateBack) {
                    QRCodeScannerScreen()
                }
            }

            else -> {
               Text("Unknown route: $key")
            }
        }
    }
}