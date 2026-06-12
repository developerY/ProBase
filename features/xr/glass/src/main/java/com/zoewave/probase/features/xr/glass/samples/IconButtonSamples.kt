package com.zoewave.probase.features.xr.glass.samples

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.xr.glimmer.GlimmerTheme
import androidx.xr.glimmer.Icon
import androidx.xr.glimmer.IconButton

@Composable
fun IconButtonSamples() {
    Row(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = {}) { Icon(Icons.Default.Favorite, null) }
        IconButton(onClick = {}) { Icon(Icons.Default.Add, null) }
        IconButton(onClick = {}) { Icon(Icons.Default.Settings, null) }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun IconButtonSamplesPreview() {
    GlimmerTheme {
        IconButtonSamples()
    }
}
