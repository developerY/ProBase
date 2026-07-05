package com.zoewave.probase.features.ai.local

import android.graphics.Bitmap
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun AiSandboxScreen(
    testBitmap: Bitmap?,
    viewModel: AiSandboxViewModel = hiltViewModel()
) {
    val consoleOutput by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "AI Edge Sandbox (Pixel 9a)",
            style = MaterialTheme.typography.headlineSmall
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = { 
                testBitmap?.let { viewModel.processTestImage(it) } 
            },
            enabled = testBitmap != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Run OCR -> Nano Pipeline")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Console Output
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = consoleOutput,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
