package com.zoewave.probase.ui.components

import android.content.Intent
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.xr.projected.experimental.ExperimentalProjectedApi
import com.zoewave.probase.features.ai.capture.ui.SmartCaptureUiRoute
import com.zoewave.probase.features.ble.ui.BluetoothLeRoute
import com.zoewave.probase.features.calendar.ui.CalendarUiRoute
import com.zoewave.probase.features.camera.ui.CameraUIRoute
import com.zoewave.probase.features.health.core.ui.HealthRoute
import com.zoewave.probase.features.health.nutrition.ui.shared.MealsUiRoute
import com.zoewave.probase.features.nav3.ui.inventory.FeatureInventory
import com.zoewave.probase.features.nav3.ui.inventory.FeatureInventoryScreen
import com.zoewave.probase.features.microphone.SpeechTestScreen
import com.zoewave.probase.features.readers.barcode.ui.BarcodeScannerScreen
import com.zoewave.probase.features.readers.nfc.ui.NfcUiRoute
import com.zoewave.probase.features.readers.qrscanner.ui.QRCodeScannerScreen
import com.zoewave.probase.features.weather.ui.WeatherUiRoute
import com.zoewave.probase.features.xr.glass.ui.GlassXRDemosPhoneRoute
import com.zoewave.probase.features.xr.xrglasses.XRGlassesActivity
import com.zoewave.probase.features.xr.xrglasses.ui.FullXRApp
import com.zoewave.probase.features.ai.configuration.ui.AiConfigurationCard
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.zoewave.probase.photodo.features.smartadvice.ui.SmartAdviceUiRoute

@OptIn(ExperimentalProjectedApi::class)
fun featureInventoryEntryProvider(
    key: NavKey,
    navigateTo: (NavKey) -> Unit,
    navigateBack: () -> Unit // ✅ Receive the back action
): NavEntry<NavKey> {

    // We wrap the content in a NavEntry, casting the key back to our specific type
    return NavEntry(key) {
        val context = LocalContext.current
        when (key) {
            is FeatureInventory.List -> {
                FeatureInventoryScreen(
                    onNavigateToHealth = { navigateTo(FeatureInventory.Health) },
                    onNavigateToHealthMeals = { navigateTo(FeatureInventory.HealthMeals) },
                    onNavigateToWeather = { navigateTo(FeatureInventory.Weather) },
                    onNavigateToBle = { navigateTo(FeatureInventory.Ble) },
                    onNavigateToNfc = { navigateTo(FeatureInventory.Nfc) },
                    onNavigateToQrScanner = { navigateTo(FeatureInventory.QrScanner) },
                    onNavigateToBarcode = { navigateTo(FeatureInventory.BarcodeScanner) },
                    onNavigateToCamera = { navigateTo(FeatureInventory.Camera) }, // ✅ Added Camera Callback
                    onNavigateToMicrophone = { navigateTo(FeatureInventory.Microphone) },
                    onNavigateToCalendar = { navigateTo(FeatureInventory.Calendar) },
                    onNavigateToSmartCapture = { navigateTo(FeatureInventory.SmartCapture) },
                    onNavigateToGlassXR = { navigateTo(FeatureInventory.GlassXR) },
                    onNavigateToFullXR = { 
                        val intent = Intent(context, XRGlassesActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    }
                )
            }

            is FeatureInventory.GlassXR -> {
                FeatureScaffold(title = "Glass XR Demos", onBack = navigateBack) {
                    GlassXRDemosPhoneRoute(onBack = navigateBack)
                }
            }

            is FeatureInventory.Health -> {
                FeatureScaffold(title = "Health", onBack = navigateBack) {
                    HealthRoute()
                }
            }

            is FeatureInventory.HealthMeals -> {
                FeatureScaffold(title = "Health Meals", onBack = navigateBack) {
                    MealsUiRoute(
                        onBack = navigateBack,
                        onNavigateToHome = { navigateBack() } // In inventory, home is the list
                    )
                }
            }

            is FeatureInventory.Weather -> {
                FeatureScaffold(title = "Weather", onBack = navigateBack) {
                    WeatherUiRoute(
                        onBack = navigateBack,
                        onNavigateToSunIntelligence = { /* Handle this if needed */ }
                    )
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

            is FeatureInventory.BarcodeScanner -> {
                FeatureScaffold(title = "Barcode Scanner", onBack = navigateBack) {
                    BarcodeScannerScreen()
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

            is FeatureInventory.Microphone -> {
                FeatureScaffold(title = "Microphone", onBack = navigateBack) {
                    SpeechTestScreen()
                }
            }

            is FeatureInventory.SmartCapture -> {
                FeatureScaffold(title = "Smart Capture", onBack = navigateBack) {
                    SmartCaptureUiRoute(
                        initialPhotoUri = null,
                        onCaptureComplete = { _ ->
                            // For testing in inventory, just go back. 
                            navigateBack()
                        },
                        onRetakeRequest = {
                            // In test inventory, "Retake" just goes back
                            navigateBack()
                        },
                        onDismiss = navigateBack
                    )
                }
            }

            is FeatureInventory.SmartAdvice -> {
                FeatureScaffold(title = "Smart Advice", onBack = navigateBack) {
                    SmartAdviceUiRoute(
                        projectId = key.projectId,
                        onDismiss = navigateBack
                    )
                }
            }

            is FeatureInventory.FullXR -> {
                FullXRApp(onClose = navigateBack)
            }

            is FeatureInventory.Settings -> {
                FeatureScaffold(title = "Settings", onBack = navigateBack) {
                    var expanded by remember { mutableStateOf(true) }
                    Column(
                        modifier = androidx.compose.ui.Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        AiConfigurationCard(
                            expanded = expanded,
                            onExpandToggle = { expanded = !expanded }
                        )
                    }
                }
            }

            else -> {
                Text("Unknown route: $key")
            }
        }
    }
}