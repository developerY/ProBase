package com.zoewave.probase.features.xr.glass

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.xr.projected.ProjectedDeviceController
import androidx.xr.projected.experimental.ExperimentalProjectedApi
import com.zoewave.probase.features.glass.translation.ui.TranslationScreen
import dagger.hilt.android.AndroidEntryPoint

/**
 * A standalone activity demonstrating the "Host Activity" pattern for live translation.
 * 
 * This activity runs on the phone, detects if glasses are connected,
 * and handles the bridging of audio/translation to the glasses UI.
 */
@OptIn(ExperimentalProjectedApi::class)
@AndroidEntryPoint
class LiveTranslationActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            var isGlassesConnected by remember { mutableStateOf(false) }

            // Check for glasses connection
            LaunchedEffect(Unit) {
                try {
                    val controller = ProjectedDeviceController.create(this@LiveTranslationActivity)
                    isGlassesConnected = controller.capabilities.isNotEmpty()
                } catch (e: Exception) {
                    isGlassesConnected = false
                }
            }

            if (isGlassesConnected) {
                // This UI is rendered to the projected display (the glasses)
                TranslationScreen()
            } else {
                // Fallback UI for the phone screen
                PhoneCompanionScreen()
            }
        }
    }
}

@Composable
fun PhoneCompanionScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "Connect your glasses to start live translation.",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PhoneCompanionScreenPreview() {
    MaterialTheme {
        PhoneCompanionScreen()
    }
}
