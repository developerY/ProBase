package com.zoewave.probase.features.qrscanner.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.zoewave.probase.features.qrscanner.R


@Composable
fun QRCodeScannerScreen() {
    val context = LocalContext.current

    // State management
    var scanResult by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Initialize the scanner once
    val scanner = remember(context) {
        val options = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE, Barcode.FORMAT_AZTEC)
            .build()
        GmsBarcodeScanning.getClient(context, options)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = {
                scanner.startScan()
                    .addOnSuccessListener { barcode ->
                        // Success: Update result and clear errors
                        scanResult = barcode.rawValue
                        errorMessage = null
                    }
                    .addOnFailureListener { e ->
                        // Failure: Show error message
                        scanResult = null
                        errorMessage = e.message ?: "Unknown error occurred"
                    }
                    .addOnCanceledListener {
                        // Canceled: Reset or show a specific message
                        scanResult = null
                        errorMessage = "Scan canceled"
                    }
            }
        ) {
            Text(stringResource(id = R.string.qr_scanner_button_text))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Logic for what text to display (Safe to use stringResource here)
        val displayText = when {
            errorMessage != null -> stringResource(R.string.qr_scanner_error_prefix, errorMessage!!)
            scanResult != null -> stringResource(R.string.qr_scanner_result_prefix, scanResult!!)
            else -> stringResource(R.string.qr_scanner_initial_result)
        }

        Text(text = displayText)
    }
}