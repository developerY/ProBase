package com.zoewave.probase.features.xr.xrglasses.ui.samples.arcore

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.xr.compose.spatial.Subspace
import androidx.xr.compose.subspace.SpatialPanel
import androidx.xr.compose.subspace.layout.SubspaceModifier

@Composable
fun UserTrackingSample() {
    Subspace {
        SpatialPanel(modifier = SubspaceModifier) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    "User Tracking (6DoF)",
                    modifier = Modifier.padding(24.dp),
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    "Monitoring your position and orientation in space.",
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
