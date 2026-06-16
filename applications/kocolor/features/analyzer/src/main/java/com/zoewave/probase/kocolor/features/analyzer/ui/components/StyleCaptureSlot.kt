package com.zoewave.probase.kocolor.features.analyzer.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.zoewave.probase.kocolor.features.analyzer.R
import com.zoewave.probase.kocolor.features.analyzer.ui.AnalyzerEvent
import com.zoewave.probase.kocolor.model.KoColorRoute

data class StyleCaptureSlotUiState(
    val title: String,
    val uri: String?,
    val modifier: Modifier = Modifier
)

@Composable
fun StyleCaptureSlot(
    uiState: StyleCaptureSlotUiState,
    onEvent: (AnalyzerEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val title = uiState.title
    val uri = uiState.uri

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { pickedUri ->
        pickedUri?.let { 
            val uriStr = it.toString()
            when {
                title.contains("Face", true) -> onEvent(AnalyzerEvent.OnFaceCaptured(uriStr))
                title.contains("Hair", true) -> onEvent(AnalyzerEvent.OnHairCaptured(uriStr))
                title.contains("Shoes", true) -> onEvent(AnalyzerEvent.OnShoesCaptured(uriStr))
                title.contains("Clothes", true) -> onEvent(AnalyzerEvent.OnClothesCaptured(uriStr))
            }
        }
    }

    var showOptions by remember { mutableStateOf(false) }

    Card(
        modifier = uiState.modifier
            .height(200.dp)
            .fillMaxWidth()
            .clickable { showOptions = true },
        colors = CardDefaults.cardColors(
            containerColor = if (uri == null) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        )
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (uri != null) {
                AsyncImage(
                    model = uri,
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Surface(
                    color = Color.Black.copy(alpha = 0.5f),
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                ) {
                    Text(
                        text = title,
                        color = Color.White,
                        modifier = Modifier.padding(4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(title, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }

    if (showOptions) {
        AlertDialog(
            onDismissRequest = { showOptions = false },
            title = { Text(stringResource(R.string.applications_kocolor_features_analyzer_capture_title_format, title)) },
            text = { Text(stringResource(R.string.applications_kocolor_features_analyzer_choose_source)) },
            confirmButton = {
                TextButton(onClick = {
                    showOptions = false
                    navTo(KoColorRoute.Camera(title.lowercase().substringAfter("your ")))
                }) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.applications_kocolor_features_analyzer_camera))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showOptions = false
                    launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.applications_kocolor_features_analyzer_gallery))
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StyleCaptureSlotPreview() {
    MaterialTheme {
        StyleCaptureSlot(
            uiState = StyleCaptureSlotUiState("Your Face", null),
            onEvent = {},
            navTo = {}
        )
    }
}
