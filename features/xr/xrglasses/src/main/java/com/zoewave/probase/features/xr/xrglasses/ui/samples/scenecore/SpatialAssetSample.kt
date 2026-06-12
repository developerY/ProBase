package com.zoewave.probase.features.xr.xrglasses.ui.samples.scenecore

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.xr.compose.subspace.SpatialPanel
import androidx.xr.compose.subspace.layout.SubspaceModifier
import androidx.xr.compose.subspace.layout.height
import androidx.xr.compose.subspace.layout.width

@Composable
fun SpatialAssetSample() {
    SpatialPanel(
        modifier = SubspaceModifier
            .width(800.dp)
            .height(600.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Native glTF Assets",
                    style = MaterialTheme.typography.headlineMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "SceneCore supports glTF 2.0 with PBR natively.",
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(32.dp))

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "// Loading 3D models in Kotlin",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "GltfModel.create(session, path)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "GltfModelEntity.create(session, model)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SpatialAssetSamplePreview() {
    MaterialTheme {
        SpatialAssetSample()
    }
}
