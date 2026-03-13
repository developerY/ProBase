package com.zoewave.probase.ashbike.wear.features.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DirectionsBike
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text

@Composable
fun AboutScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Icon (I dropped a default bike icon in, but you should
        // swap this to your actual painterResource AshBike logo!)
        Icon(
            imageVector = Icons.Rounded.DirectionsBike,
            contentDescription = "AshBike Logo",
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colors.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "AshBike Wear",
            style = MaterialTheme.typography.title2,
            color = MaterialTheme.colors.onBackground
        )

        Text(
            text = "Version 1.0.0",
            style = MaterialTheme.typography.body2,
            color = MaterialTheme.colors.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "© 2026 🌊 ZoeWave",
            style = MaterialTheme.typography.caption3,
            color = MaterialTheme.colors.onSurfaceVariant
        )
    }
}

@Preview(
    device = Devices.WEAR_OS_SMALL_ROUND,
    showSystemUi = true,
    backgroundColor = 0xFF000000,
    showBackground = true,
    name = "About Screen Preview"
)
@Composable
fun AboutScreenPreview() {
    // Wrap in your AshBikeTheme if you have custom colors defined!
    MaterialTheme {
        AboutScreen()
    }
}