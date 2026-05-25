package com.zoewave.probase.features.xr.glass.samples

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.xr.glimmer.Card
import androidx.xr.glimmer.GlimmerTheme
import androidx.xr.glimmer.Text
import androidx.xr.glimmer.stack.VerticalStack

@Composable
fun StacksSamples() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        VerticalStack(
            modifier = Modifier.padding(16.dp)
        ) {
            item {
                Card(title = { Text("Stack Item 1") }) {
                    Text("Only one item in a stack is visible at a time.")
                }
            }
            item {
                Card(title = { Text("Stack Item 2") }) {
                    Text("You can navigate between items in the stack.")
                }
            }
        }
    }
}
