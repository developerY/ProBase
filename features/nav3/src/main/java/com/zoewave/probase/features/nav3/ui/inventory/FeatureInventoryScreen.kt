package com.zoewave.probase.features.nav3.ui.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeatureInventoryScreen(
    onNavigateToHealth: () -> Unit,
    onNavigateToHealthMeals: () -> Unit,
    onNavigateToWeather: () -> Unit,
    onNavigateToBle: () -> Unit,
    onNavigateToNfc: () -> Unit,
    onNavigateToQrScanner: () -> Unit,
    onNavigateToBarcode: () -> Unit,
    onNavigateToCamera: () -> Unit,
    onNavigateToMicrophone: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToSmartCapture: () -> Unit,
    onNavigateToGlassXR: () -> Unit,
    onNavigateToFullXR: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("System Features Inventory") }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                FeatureCard(
                    title = "Health & Metrics",
                    description = "Google Health Connect integration, steps, and activity.",
                    icon = Icons.Default.Favorite,
                    onClick = onNavigateToHealth
                )
            }
            item {
                FeatureCard(
                    title = "Health Meals",
                    description = "Manage meals, nutrition, and capture food via camera.",
                    icon = Icons.Default.Favorite, // Use same icon or a different one
                    onClick = onNavigateToHealthMeals
                )
            }
            item {
                FeatureCard(
                    title = "Weather",
                    description = "Live weather updates and forecasts.",
                    icon = Icons.Default.Cloud,
                    onClick = onNavigateToWeather
                )
            }
            item {
                FeatureCard(
                    title = "Bluetooth / BLE",
                    description = "Scan for devices, connect, and read characteristics.",
                    icon = Icons.Default.Bluetooth, // Uses the built-in Bluetooth icon
                    onClick = onNavigateToBle
                )
            }
            item {
                FeatureCard(
                    title = "NFC",
                    description = "Read and write NFC tags.",
                    icon = Icons.Default.Nfc, // Uses the built-in Bluetooth icon
                    onClick = onNavigateToNfc

                )
            }
            item {
                FeatureCard(
                    title = "QR Scanner",
                    description = "Scan QR codes and barcodes.",
                    icon = Icons.Default.QrCodeScanner, // Uses the built-in Bluetooth icon
                    onClick = onNavigateToQrScanner
                )
            }
            item {
                FeatureCard(
                    title = "Barcode Scanner",
                    description = "Universal barcode scanning module.",
                    icon = Icons.Default.QrCodeScanner, // Use QrCodeScanner for now or find Barcode equivalent
                    onClick = onNavigateToBarcode
                )
            }
            item {
                FeatureCard(
                    title = "Camera",
                    description = "Capture photos and record videos.",
                    icon = Icons.Default.CameraAlt,
                    onClick = onNavigateToCamera
                )
            }
            item {
                FeatureCard(
                    title = "Microphone",
                    description = "Test speech recognition and microphone input.",
                    icon = Icons.Default.Mic,
                    onClick = onNavigateToMicrophone
                )
            }
            item {
                FeatureCard(
                    title = "Calendar",
                    description = "Interact with the system calendar.",
                    icon = Icons.Default.CalendarMonth,
                    onClick = onNavigateToCalendar
                )
            }
            item {
                FeatureCard(
                    title = "Smart Capture",
                    description = "Extract tasks from photos using AI.",
                    icon = Icons.Default.AutoAwesome,
                    onClick = onNavigateToSmartCapture
                )
            }
            item {
                FeatureCard(
                    title = "Glass XR Demos",
                    description = "Projected UI samples for display glasses.",
                    icon = Icons.Default.AutoAwesome, // You can find a better icon later
                    onClick = onNavigateToGlassXR
                )
            }
            item {
                FeatureCard(
                    title = "Full XR Showcase",
                    description = "Spatial UI, 3D Models, and Perception for XR Glasses.",
                    icon = Icons.Default.AutoAwesome,
                    onClick = onNavigateToFullXR
                )
            }

        }
    }
}

@Composable
fun FeatureCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}