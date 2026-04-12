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
// ✅ Add your Camera route import here
import com.zoewave.probase.features.camera.ui.CameraUIRoute
import com.zoewave.probase.features.calendar.ui.CalendarUiRoute
import com.zoewave.probase.features.smartcapture.ui.SmartCaptureUiRoute

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
                    onNavigateToQrScanner = { navigateTo(FeatureInventory.QrScanner) },
                    onNavigateToCamera = { navigateTo(FeatureInventory.Camera) }, // ✅ Added Camera Callback
                    onNavigateToCalendar = { navigateTo(FeatureInventory.Calendar) },
                    onNavigateToSmartCapture = { navigateTo(FeatureInventory.SmartCapture) }
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

            // ✅ Added Camera Route Branch
            is FeatureInventory.Camera -> {
                FeatureScaffold(title = "Camera", onBack = navigateBack) {
                    CameraUIRoute(
                        navTo = { route -> /* Handle internal camera navigation if needed */ }
                    )
                }
            }

            is FeatureInventory.Calendar -> {
                FeatureScaffold(title = "Calendar", onBack = navigateBack) {
                    CalendarUiRoute()
                }
            }

            is FeatureInventory.SmartCapture -> {
                FeatureScaffold(title = "Smart Capture", onBack = navigateBack) {
                    SmartCaptureUiRoute(
                        onDismiss = navigateBack
                    )
                }
            }

            else -> {
                Text("Unknown route: $key")
            }
        }
    }
}