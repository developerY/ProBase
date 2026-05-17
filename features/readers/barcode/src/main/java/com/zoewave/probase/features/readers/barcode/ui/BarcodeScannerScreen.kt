package com.zoewave.probase.features.readers.barcode.ui

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
import com.zoewave.probase.features.readers.barcode.R

@Composable
fun BarcodeScannerScreen(
    onCodeScanned: (String) -> Unit = {}
) {
    val context = LocalContext.current

    var scanResult by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scanner = remember(context) {
        val options = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
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
                        scanResult = barcode.rawValue
                        errorMessage = null
                        barcode.rawValue?.let { onCodeScanned(it) }
                    }
                    .addOnFailureListener { e ->
                        scanResult = null
                        errorMessage = e.message ?: "Unknown error occurred"
                    }
            }
        ) {
            Text(stringResource(id = R.string.features_readers_barcode_scan_button))
        }

        Spacer(modifier = Modifier.height(16.dp))

        val displayText = when {
            errorMessage != null -> stringResource(R.string.features_readers_barcode_error_prefix, errorMessage!!)
            scanResult != null -> stringResource(R.string.features_readers_barcode_result_prefix, scanResult!!)
            else -> stringResource(R.string.features_readers_barcode_initial_result)
        }

        Text(text = displayText)
    }
}