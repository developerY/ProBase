package com.zoewave.probase.features.xr.xrglasses.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.ui.Alignment
import androidx.xr.compose.platform.LocalSession
import androidx.xr.compose.spatial.Orbiter
import androidx.xr.compose.spatial.OrbiterAnchorPoint
import androidx.xr.compose.spatial.Subspace
import com.zoewave.probase.features.xr.xrglasses.ui.samples.arcore.FaceEyeTrackingSample
import com.zoewave.probase.features.xr.xrglasses.ui.samples.arcore.PlaneDetectionSample
import com.zoewave.probase.features.xr.xrglasses.ui.samples.arcore.UserTrackingSample
import com.zoewave.probase.features.xr.xrglasses.ui.samples.compose.OrbiterSample
import com.zoewave.probase.features.xr.xrglasses.ui.samples.compose.SpatialDialogSample
import com.zoewave.probase.features.xr.xrglasses.ui.samples.compose.SpatialElevationSample
import com.zoewave.probase.features.xr.xrglasses.ui.samples.compose.SpatialLayoutSample
import com.zoewave.probase.features.xr.xrglasses.ui.samples.compose.SpatialPanelSample
import com.zoewave.probase.features.xr.xrglasses.ui.samples.compose.SpatialPopupSample
import com.zoewave.probase.features.xr.xrglasses.ui.samples.compose.SubspaceModifierSample
import com.zoewave.probase.features.xr.xrglasses.ui.samples.compose.SubspaceSample
import com.zoewave.probase.features.xr.xrglasses.ui.samples.scenecore.AnchoringSample
import com.zoewave.probase.features.xr.xrglasses.ui.samples.scenecore.GltfModelSample
import com.zoewave.probase.features.xr.xrglasses.ui.samples.scenecore.TransformSample

sealed class XRSampleCategory(val title: String) {
    data object Compose : XRSampleCategory("Compose for XR")
    data object SceneCore : XRSampleCategory("SceneCore 3D")
    data object ARCore : XRSampleCategory("ARCore Perception")
    data object None : XRSampleCategory("Menu")
}

sealed class XRSample(val title: String) {
    data object None : XRSample("None")
    // Compose
    data object SpatialPanel : XRSample("Spatial Panel")
    data object Orbiter : XRSample("Orbiter")
    data object Subspace : XRSample("Subspace")
    data object SpatialDialog : XRSample("Spatial Dialog")
    data object SpatialPopup : XRSample("Spatial Popup")
    data object SpatialElevation : XRSample("Spatial Elevation")
    data object SpatialLayout : XRSample("Spatial Layout")
    data object SubspaceModifier : XRSample("Subspace Modifier")
    // SceneCore
    data object GltfModel : XRSample("GLTF Model")
    data object Anchoring : XRSample("Anchoring")
    data object Transform : XRSample("Transform")
    // ARCore
    data object PlaneDetection : XRSample("Plane Detection")
    data object UserTracking : XRSample("User Tracking")
    data object FaceEyeTracking : XRSample("Face & Eye Tracking")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullXRApp(onClose: () -> Unit) {
    var currentCategory by remember { mutableStateOf<XRSampleCategory>(XRSampleCategory.None) }
    var currentSample by remember { mutableStateOf<XRSample>(XRSample.None) }
    val session = LocalSession.current

    if (currentSample != XRSample.None) {
        // 2D Fallback / Control UI
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Running: ${currentSample.title}") },
                    navigationIcon = {
                        IconButton(onClick = { currentSample = XRSample.None }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier.padding(innerPadding).fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("The sample is rendering in 3D Space.", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(16.dp))
                
                if (session != null) {
                    Text("The XR session is active. Use the emulator's 'Home' and 'Immersive' toggles or your app's programmatic mode requests to transition.", style = MaterialTheme.typography.bodySmall)
                } else {
                    Text("XR Session not detected. Ensure you are on an XR device.", color = MaterialTheme.colorScheme.error)
                }
            }
        }

        // 3D Spatial UI
        Subspace {
            // Render the actual sample
            when (currentSample) {
                XRSample.SpatialPanel -> SpatialPanelSample()
                XRSample.Orbiter -> OrbiterSample()
                XRSample.Subspace -> SubspaceSample()
                XRSample.SpatialDialog -> SpatialDialogSample()
                XRSample.SpatialPopup -> SpatialPopupSample()
                XRSample.SpatialElevation -> SpatialElevationSample()
                XRSample.SpatialLayout -> SpatialLayoutSample()
                XRSample.SubspaceModifier -> SubspaceModifierSample()
                XRSample.GltfModel -> GltfModelSample()
                XRSample.Anchoring -> AnchoringSample()
                XRSample.Transform -> TransformSample()
                XRSample.PlaneDetection -> PlaneDetectionSample()
                XRSample.UserTracking -> UserTrackingSample()
                XRSample.FaceEyeTracking -> FaceEyeTrackingSample()
                else -> {}
            }

            // Centralized Spatial Exit Orbiter (floats in 3D)
            Orbiter(
                anchorPoint = OrbiterAnchorPoint.TopStart,
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = CircleShape,
                    shadowElevation = 12.dp
                ) {
                    IconButton(onClick = { currentSample = XRSample.None }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack, 
                            contentDescription = "Exit Sample",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
        return
    }

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
                XRSampleCategory.Compose -> ComposeXRSamples(onSampleSelected = { currentSample = it })
                XRSampleCategory.SceneCore -> SceneCoreSamples(onSampleSelected = { currentSample = it })
                XRSampleCategory.ARCore -> ARCoreSamples(onSampleSelected = { currentSample = it })
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FullXRAppPreview() {
    MaterialTheme {
        FullXRApp(onClose = {})
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
fun ComposeXRSamples(onSampleSelected: (XRSample) -> Unit) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Spatial UI Components", style = MaterialTheme.typography.headlineSmall)
        ListItem(
            headlineContent = { Text("Spatial Panel") }, 
            supportingContent = { Text("Traditional 2D UI in 3D space.") },
            modifier = Modifier.clickable { onSampleSelected(XRSample.SpatialPanel) }
        )
        ListItem(
            headlineContent = { Text("Orbiter") }, 
            supportingContent = { Text("Floating elements around a panel.") },
            modifier = Modifier.clickable { onSampleSelected(XRSample.Orbiter) }
        )
        ListItem(
            headlineContent = { Text("Subspace") }, 
            supportingContent = { Text("Defining 3D volumes.") },
            modifier = Modifier.clickable { onSampleSelected(XRSample.Subspace) }
        )
        ListItem(
            headlineContent = { Text("Spatial Dialog") }, 
            supportingContent = { Text("3D dialogs with depth.") },
            modifier = Modifier.clickable { onSampleSelected(XRSample.SpatialDialog) }
        )
        ListItem(
            headlineContent = { Text("Spatial Popup") }, 
            supportingContent = { Text("Contextual menus in 3D.") },
            modifier = Modifier.clickable { onSampleSelected(XRSample.SpatialPopup) }
        )
        ListItem(
            headlineContent = { Text("Spatial Elevation") }, 
            supportingContent = { Text("Z-axis depth levels.") },
            modifier = Modifier.clickable { onSampleSelected(XRSample.SpatialElevation) }
        )
        ListItem(
            headlineContent = { Text("Spatial Layout") }, 
            supportingContent = { Text("Row, Column, and Curved Row.") },
            modifier = Modifier.clickable { onSampleSelected(XRSample.SpatialLayout) }
        )
        ListItem(
            headlineContent = { Text("Subspace Modifiers") }, 
            supportingContent = { Text("Offset, Rotate, and Movable.") },
            modifier = Modifier.clickable { onSampleSelected(XRSample.SubspaceModifier) }
        )
    }
}

@Composable
fun SceneCoreSamples(onSampleSelected: (XRSample) -> Unit) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("SceneCore 3D Manipulation", style = MaterialTheme.typography.headlineSmall)
        ListItem(
            headlineContent = { Text("GLTF Model Viewer") }, 
            supportingContent = { Text("Load .gltf / .glb assets.") },
            modifier = Modifier.clickable { onSampleSelected(XRSample.GltfModel) }
        )
        ListItem(
            headlineContent = { Text("Surface Anchoring") }, 
            supportingContent = { Text("Place objects on floor/tables.") },
            modifier = Modifier.clickable { onSampleSelected(XRSample.Anchoring) }
        )
        ListItem(
            headlineContent = { Text("Transform System") }, 
            supportingContent = { Text("Rotate, Scale, Move 3D entities.") },
            modifier = Modifier.clickable { onSampleSelected(XRSample.Transform) }
        )
    }
}

@Composable
fun ARCoreSamples(onSampleSelected: (XRSample) -> Unit) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("ARCore Perception Features", style = MaterialTheme.typography.headlineSmall)
        ListItem(
            headlineContent = { Text("Plane Detection") }, 
            supportingContent = { Text("Visualize detected surfaces.") },
            modifier = Modifier.clickable { onSampleSelected(XRSample.PlaneDetection) }
        )
        ListItem(
            headlineContent = { Text("User Tracking") }, 
            supportingContent = { Text("Position and orientation monitoring.") },
            modifier = Modifier.clickable { onSampleSelected(XRSample.UserTracking) }
        )
        ListItem(
            headlineContent = { Text("Face & Eye Tracking") }, 
            supportingContent = { Text("Immersive gaze-based interaction.") },
            modifier = Modifier.clickable { onSampleSelected(XRSample.FaceEyeTracking) }
        )
    }
}
