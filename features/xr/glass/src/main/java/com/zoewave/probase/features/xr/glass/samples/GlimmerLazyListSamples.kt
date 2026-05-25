package com.zoewave.probase.features.xr.glass.samples

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.xr.glimmer.ListItem
import androidx.xr.glimmer.Text
import androidx.xr.glimmer.list.GlimmerLazyColumn

@Composable
fun GlimmerLazyListSamples() {
    GlimmerLazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Text("Scrollable List")
        }
        items(20) { index ->
            ListItem(onClick = {}) {
                Text("Item #$index")
            }
        }
    }
}
