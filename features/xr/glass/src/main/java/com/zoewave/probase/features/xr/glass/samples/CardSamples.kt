package com.zoewave.probase.features.xr.glass.samples

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.xr.glimmer.Button
import androidx.xr.glimmer.Card
import androidx.xr.glimmer.GlimmerTheme
import androidx.xr.glimmer.Text

@Composable
fun CardSamples() {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Card(
            title = { Text("Standard Card") }
        ) {
            Text("This is a standard Glimmer card with some content inside.")
        }

        Card(
            title = { Text("Interactive Card") },
            onClick = { /* Handle card click */ }
        ) {
            Text("This card is interactive and provides visual feedback when focused/clicked.")
        }

        Card(
            title = { Text("Card with Action") },
            action = { Button(onClick = {}) { Text("Action") } }
        ) {
            Text("This card has an explicit action button in the header.")
        }
    }
}
