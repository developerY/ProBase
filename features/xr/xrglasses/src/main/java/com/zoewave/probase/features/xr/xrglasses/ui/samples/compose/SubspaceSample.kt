package com.zoewave.probase.features.xr.xrglasses.ui.samples.compose

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.xr.compose.subspace.SpatialColumn
import androidx.xr.compose.subspace.SpatialPanel
import androidx.xr.compose.subspace.layout.SubspaceModifier
import androidx.xr.compose.subspace.layout.height
import androidx.xr.compose.subspace.layout.width

@Composable
fun SubspaceSample() {
    SpatialColumn(
        modifier = SubspaceModifier
            .width(800.dp)
            .height(600.dp)
    ) {
        SpatialPanel(modifier = SubspaceModifier.width(800.dp).height(280.dp)) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    "Top Spatial Element",
                    modifier = Modifier.padding(24.dp),
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }

        SpatialPanel(modifier = SubspaceModifier.width(800.dp).height(280.dp)) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(
                    "Bottom Spatial Element",
                    modifier = Modifier.padding(24.dp),
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}
