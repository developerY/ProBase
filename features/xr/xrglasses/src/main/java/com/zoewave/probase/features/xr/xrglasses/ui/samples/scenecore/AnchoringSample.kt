package com.zoewave.probase.features.xr.xrglasses.ui.samples.scenecore

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.xr.compose.subspace.SpatialPanel
import androidx.xr.compose.subspace.layout.SubspaceModifier
import androidx.xr.compose.subspace.layout.height
import androidx.xr.compose.subspace.layout.width

@Composable
fun AnchoringSample() {
    SpatialPanel(
        modifier = SubspaceModifier
            .width(800.dp)
            .height(600.dp)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = MaterialTheme.shapes.medium
        ) {
            Text(
                "Surface Anchoring",
                modifier = Modifier.padding(24.dp),
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                "This sample demonstrates how to anchor 3D objects to real-world surfaces like floors or tables.",
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
