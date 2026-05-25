package com.zoewave.probase.features.xr.xrglasses.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

sealed class XRSampleCategory(val title: String) {
    data object Compose : XRSampleCategory("Compose for XR")
    data object SceneCore : XRSampleCategory("SceneCore 3D")
    data object ARCore : XRSampleCategory("ARCore Perception")
    data object None : XRSampleCategory("Menu")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullXRApp(onClose: () -> Unit) {
    var currentCategory by remember { mutableStateOf<XRSampleCategory>(XRSampleCategory.None) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentCategory.title) },
                navigationIcon = {
                    IconButton(onClick = { 
                        if (currentCategory == XRSampleCategory.None) onClose() else currentCategory = XRSampleCategory.None 
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (currentCategory) {
                XRSampleCategory.None -> FullXRMenu(onCategorySelected = { currentCategory = it })
                XRSampleCategory.Compose -> ComposeXRSamples()
                XRSampleCategory.SceneCore -> SceneCoreSamples()
                XRSampleCategory.ARCore -> ARCoreSamples()
            }
        }
    }
}

@Composable
fun FullXRMenu(onCategorySelected: (XRSampleCategory) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        XRMenuCard(
            title = "Compose for XR",
            description = "Spatial Panels, Orbiters, and Subspaces.",
            onClick = { onCategorySelected(XRSampleCategory.Compose) }
        )
        XRMenuCard(
            title = "SceneCore 3D",
            description = "GLTF loading, Anchoring, and Scene Graph manipulation.",
            onClick = { onCategorySelected(XRSampleCategory.SceneCore) }
        )
        XRMenuCard(
            title = "ARCore Perception",
            description = "Spatial tracking, plane detection, face and eye tracking.",
            onClick = { onCategorySelected(XRSampleCategory.ARCore) }
        )
    }
}

@Composable
fun XRMenuCard(title: String, description: String, onClick: () -> Unit) {
    ElevatedCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun ComposeXRSamples() {
    // Placeholder for actual XR Compose components
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Spatial UI Components", style = MaterialTheme.typography.headlineSmall)
        ListItem(headlineContent = { Text("Spatial Panel") }, supportingContent = { Text("Traditional 2D UI in 3D space.") })
        ListItem(headlineContent = { Text("Orbiter") }, supportingContent = { Text("Floating elements around a panel.") })
        ListItem(headlineContent = { Text("Subspace") }, supportingContent = { Text("Defining 3D volumes.") })
    }
}

@Composable
fun SceneCoreSamples() {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("SceneCore 3D Manipulation", style = MaterialTheme.typography.headlineSmall)
        ListItem(headlineContent = { Text("GLTF Model Viewer") }, supportingContent = { Text("Load .gltf / .glb assets.") })
        ListItem(headlineContent = { Text("Surface Anchoring") }, supportingContent = { Text("Place objects on floor/tables.") })
        ListItem(headlineContent = { Text("Transform System") }, supportingContent = { Text("Rotate, Scale, Move 3D entities.") })
    }
}

@Composable
fun ARCoreSamples() {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("ARCore Perception Features", style = MaterialTheme.typography.headlineSmall)
        ListItem(headlineContent = { Text("Plane Detection") }, supportingContent = { Text("Visualize detected surfaces.") })
        ListItem(headlineContent = { Text("User Tracking") }, supportingContent = { Text("Position and orientation monitoring.") })
        ListItem(headlineContent = { Text("Face & Eye Tracking") }, supportingContent = { Text("Immersive gaze-based interaction.") })
    }
}
