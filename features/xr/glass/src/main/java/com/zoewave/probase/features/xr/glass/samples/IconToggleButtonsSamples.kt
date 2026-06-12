package com.zoewave.probase.features.xr.glass.samples

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.xr.glimmer.GlimmerTheme
import androidx.xr.glimmer.Icon
import androidx.xr.glimmer.IconToggleButton

@Composable
fun IconToggleButtonsSamples() {
    var isMicOn by remember { mutableStateOf(true) }
    var isNotifyOn by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconToggleButton(
            checked = isMicOn,
            onCheckedChange = { isMicOn = it }
        ) {
            Icon(if (isMicOn) Icons.Default.Mic else Icons.Default.MicOff, null)
        }
        
        IconToggleButton(
            checked = isNotifyOn,
            onCheckedChange = { isNotifyOn = it }
        ) {
            Icon(if (isNotifyOn) Icons.Default.Notifications else Icons.Default.NotificationsOff, null)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun IconToggleButtonsSamplesPreview() {
    GlimmerTheme {
        IconToggleButtonsSamples()
    }
}
