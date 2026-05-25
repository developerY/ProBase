package com.zoewave.probase.features.xr.xrglasses.ui.samples.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.xr.compose.subspace.SpatialColumn
import androidx.xr.compose.subspace.SpatialCurvedRow
import androidx.xr.compose.subspace.SpatialPanel
import androidx.xr.compose.subspace.SpatialRow
import androidx.xr.compose.subspace.SpatialSpacer
import androidx.xr.compose.subspace.layout.SubspaceModifier
import androidx.xr.compose.subspace.layout.height
import androidx.xr.compose.subspace.layout.width

@Composable
fun SpatialLayoutSample() {
    SpatialColumn(
        modifier = SubspaceModifier
            .width(800.dp)
            .height(600.dp)
    ) {
        // Label for the layout
        SpatialPanel(modifier = SubspaceModifier.width(800.dp).height(100.dp)) {
            Surface(
                color = Color.Transparent,
                modifier = Modifier.fillMaxSize()
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "Spatial Layouts (Row, Column, Spacer)",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        SpatialSpacer(modifier = SubspaceModifier.height(20.dp))

        // Standard Spatial Row
        SpatialRow {
            LayoutPanel("Panel A", MaterialTheme.colorScheme.primaryContainer)
            SpatialSpacer(modifier = SubspaceModifier.width(20.dp))
            LayoutPanel("Panel B", MaterialTheme.colorScheme.secondaryContainer)
        }

        SpatialSpacer(modifier = SubspaceModifier.height(40.dp))

        // Label for curved row
        SpatialPanel(modifier = SubspaceModifier.width(800.dp).height(80.dp)) {
            Surface(
                color = Color.Transparent,
                modifier = Modifier.fillMaxSize()
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "Spatial Curved Row (Cockpit Layout)",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        // Curved Cockpit Row
        SpatialCurvedRow(curveRadius = 1000.dp) {
            LayoutPanel("Left", MaterialTheme.colorScheme.tertiaryContainer)
            SpatialSpacer(modifier = SubspaceModifier.width(20.dp))
            LayoutPanel("Center", MaterialTheme.colorScheme.surfaceVariant)
            SpatialSpacer(modifier = SubspaceModifier.width(20.dp))
            LayoutPanel("Right", MaterialTheme.colorScheme.primaryContainer)
        }
    }
}

@Composable
private fun LayoutPanel(title: String, color: Color) {
    SpatialPanel(modifier = SubspaceModifier.width(200.dp).height(150.dp)) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = color,
            shape = MaterialTheme.shapes.medium
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(title, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
