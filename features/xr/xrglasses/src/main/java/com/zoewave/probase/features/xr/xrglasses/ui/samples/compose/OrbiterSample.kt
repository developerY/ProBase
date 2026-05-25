package com.zoewave.probase.features.xr.xrglasses.ui.samples.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.xr.compose.spatial.Orbiter
import androidx.xr.compose.spatial.OrbiterAnchorPoint
import androidx.xr.compose.spatial.Subspace
import androidx.xr.compose.subspace.SpatialPanel
import androidx.xr.compose.subspace.layout.SubspaceModifier

@Composable
fun OrbiterSample() {
    Subspace {
        SpatialPanel(modifier = SubspaceModifier) {
            Surface(
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.medium
            ) {
                Column(modifier = Modifier.padding(48.dp)) {
                    Text(
                        text = "Panel with Orbiters",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = "Look at the edges of this panel!",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
            
            // Top End Orbiter for Settings
            Orbiter(
                anchorPoint = OrbiterAnchorPoint.TopEnd,
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.extraLarge,
                    shadowElevation = 4.dp
                ) {
                    IconButton(onClick = { /* Action */ }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            }

            // Bottom Orbiter for Info
            Orbiter(
                anchorPoint = OrbiterAnchorPoint.Bottom,
            ) {
                FilterChip(
                    selected = true,
                    onClick = { /* Action */ },
                    label = { Text("Details") },
                    leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) }
                )
            }
        }
    }
}
