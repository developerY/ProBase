package com.zoewave.probase.features.xr.xrglasses.ui.samples.arcore

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.xr.arcore.Anchor
import androidx.xr.compose.platform.LocalSession
import androidx.xr.compose.subspace.SpatialPanel
import androidx.xr.compose.subspace.layout.SubspaceModifier
import androidx.xr.compose.subspace.layout.height
import androidx.xr.compose.subspace.layout.width
import java.util.UUID

@Composable
fun SpatialMemorySample() {
    val session = LocalSession.current
    var lastPersistedUuid by remember { mutableStateOf<UUID?>(null) }
    var statusMessage by remember { mutableStateOf("Ready to persist spatial notes.") }

    SpatialPanel(
        modifier = SubspaceModifier
            .width(800.dp)
            .height(600.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Spatial Memory (Persistence)",
                    style = MaterialTheme.typography.headlineMedium
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = statusMessage,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(onClick = {
                        if (session != null) {
                            // In a real app, you'd use a meaningful Pose
                            // val anchor = Anchor.create(session, somePose)
                            // lastPersistedUuid = anchor.persist()
                            statusMessage = "Note persisted to spatial map!"
                        }
                    }) {
                        Text("Drop Spatial Note")
                    }

                    Button(
                        onClick = {
                            statusMessage = "Scanning room to re-localize note..."
                            // session?.let { Anchor.load(it, uuid) }
                        },
                        enabled = true
                    ) {
                        Text("Restore Notes")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SpatialMemorySamplePreview() {
    MaterialTheme {
        SpatialMemorySample()
    }
}
