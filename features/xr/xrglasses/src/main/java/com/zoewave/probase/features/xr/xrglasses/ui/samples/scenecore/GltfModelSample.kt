package com.zoewave.probase.features.xr.xrglasses.ui.samples.scenecore

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.xr.compose.platform.LocalSession
import androidx.xr.compose.subspace.SpatialPanel
import androidx.xr.compose.subspace.layout.SubspaceModifier
import androidx.xr.compose.subspace.layout.height
import androidx.xr.compose.subspace.layout.width

@Composable
fun GltfModelSample() {
    val session = LocalSession.current
    
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
                "GLTF Model Sample",
                modifier = Modifier.padding(24.dp),
                style = MaterialTheme.typography.headlineSmall
            )
            if (session == null) {
                Text("Session not active. Models require an XR session.")
            } else {
                Text("XR Session Active - Ready to load models via GltfModelEntity.")
            }
        }
    }
}
