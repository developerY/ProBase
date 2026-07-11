package com.zoewave.probase.features.xr.glass

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.xr.glimmer.Button
import androidx.xr.glimmer.Card
import androidx.xr.glimmer.GlimmerTheme
import androidx.xr.glimmer.Icon
import androidx.xr.glimmer.Text
import androidx.xr.projected.experimental.ExperimentalProjectedApi

/**
 * A "System Alert" activity that covers the main app.
 * Used to demonstrate the ON_PAUSE lifecycle state on AI Glasses.
 */
@OptIn(ExperimentalProjectedApi::class)
class SystemAlertActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // This activity should be translucent so we can see the background app
        setContent {
            GlimmerTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    AlertContent(onDismiss = { finish() })
                }
            }
        }
    }
}

@Composable
private fun AlertContent(onDismiss: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(0.8f),
        title = { Text("System Alert") },
        leadingIcon = {
            Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Yellow)
        }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "This activity has pushed the background app to ON_PAUSE.",
                style = GlimmerTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(24.dp))
            Button(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                Text("Dismiss")
            }
        }
    }
}
