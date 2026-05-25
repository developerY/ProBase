package com.zoewave.probase.features.xr.xrglasses.ui.samples.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.xr.compose.spatial.SpatialDialog
import androidx.xr.compose.subspace.SpatialPanel
import androidx.xr.compose.subspace.layout.SubspaceModifier
import androidx.xr.compose.subspace.layout.height as subspaceHeight
import androidx.xr.compose.subspace.layout.width as subspaceWidth

@Composable
fun SpatialDialogSample() {
    var showDialog by remember { mutableStateOf(false) }

    SpatialPanel(
        modifier = SubspaceModifier
            .subspaceWidth(800.dp)
            .subspaceHeight(600.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Spatial Dialog Sample",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "SpatialDialog appears in front of the main panel in 3D.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 16.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.height(24.dp))
                Button(onClick = { showDialog = true }) {
                    Text("Show Spatial Dialog")
                }
            }
        }

        if (showDialog) {
            SpatialDialog(onDismissRequest = { showDialog = false }) {
                Surface(
                    modifier = Modifier.width(400.dp).height(300.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.medium,
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Spatial Dialog", style = MaterialTheme.typography.headlineMedium)
                        Spacer(Modifier.height(16.dp))
                        Text("This dialog has 3D depth and will fall back to a 2D Dialog on non-XR devices.")
                        Spacer(Modifier.height(24.dp))
                        Button(onClick = { showDialog = false }) {
                            Text("Dismiss")
                        }
                    }
                }
            }
        }
    }
}
