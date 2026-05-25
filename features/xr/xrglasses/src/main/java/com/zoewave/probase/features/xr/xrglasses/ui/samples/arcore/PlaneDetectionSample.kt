package com.zoewave.probase.features.xr.xrglasses.ui.samples.arcore

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.xr.arcore.Plane
import androidx.xr.compose.platform.LocalSession
import androidx.xr.compose.spatial.Subspace
import androidx.xr.compose.subspace.SpatialPanel
import androidx.xr.compose.subspace.layout.SubspaceModifier

@Composable
fun PlaneDetectionSample() {
    val session = LocalSession.current
    var planeCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(session) {
        if (session != null) {
            Plane.subscribe(session).collect { planes ->
                planeCount = planes.size
            }
        }
    }

    Subspace {
        SpatialPanel(modifier = SubspaceModifier) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    "Plane Detection",
                    modifier = Modifier.padding(24.dp),
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    "Detected Planes: $planeCount",
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
