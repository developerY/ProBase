package com.zoewave.probase.kocolor.features.analyzer.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.zoewave.probase.kocolor.model.FashionAdvice
import com.zoewave.probase.kocolor.model.KoColorRoute
import com.zoewave.probase.kocolor.model.SeasonalType
import com.zoewave.probase.kocolor.model.Undertone

@Composable
fun AnalyzerUiRoute(
    uiState: Unit = Unit,
    onEvent: (Unit) -> Unit = {},
    navTo: (KoColorRoute) -> Unit
) {
    val viewModel: AnalyzerViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    AnalyzerScreen(
        uiState = state,
        onEvent = viewModel::onEvent,
        navTo = navTo
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyzerScreen(
    uiState: AnalyzerScreenUiState,
    onEvent: (AnalyzerEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fashion Analyzer") },
                navigationIcon = {
                    IconButton(onClick = { navTo(KoColorRoute.Back) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState.analyzerState) {
                is AnalyzerUiState.Idle -> {
                    StyleCaptureState(
                        uiState = uiState,
                        onEvent = onEvent,
                        navTo = navTo
                    )
                }
                is AnalyzerUiState.Loading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Gemini is analyzing your style...")
                    }
                }
                is AnalyzerUiState.Success -> {
                    AnalysisResultScreen(
                        uiState = state.advice,
                        onEvent = { event ->
                            if (event is AnalyzerEvent.OnSaveClicked) {
                                onEvent(event)
                                navTo(KoColorRoute.Back)
                            } else {
                                onEvent(event)
                            }
                        },
                        navTo = navTo
                    )
                }
                is AnalyzerUiState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                        Button(onClick = { onEvent(AnalyzerEvent.OnResetClicked) }, modifier = Modifier.padding(top = 16.dp)) {
                            Text("Try Again")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StyleCaptureState(
    uiState: AnalyzerScreenUiState,
    onEvent: (AnalyzerEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Capture up to 4 images for a holistic style analysis",
            style = MaterialTheme.typography.titleMedium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Column(
            modifier = Modifier.height(400.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StyleCaptureSlot(
                    uiState = "Your Face" to uiState.faceUri,
                    onEvent = { uri -> onEvent(AnalyzerEvent.OnFaceCaptured(uri)) },
                    navTo = { navTo(KoColorRoute.Camera("face")) }
                )
                StyleCaptureSlot(
                    uiState = "Your Hair" to uiState.hairUri,
                    onEvent = { uri -> onEvent(AnalyzerEvent.OnHairCaptured(uri)) },
                    navTo = { navTo(KoColorRoute.Camera("hair")) }
                )
            }
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StyleCaptureSlot(
                    uiState = "Your Shoes" to uiState.shoesUri,
                    onEvent = { uri -> onEvent(AnalyzerEvent.OnShoesCaptured(uri)) },
                    navTo = { navTo(KoColorRoute.Camera("shoes")) }
                )
                StyleCaptureSlot(
                    uiState = "Your Clothes" to uiState.clothesUri,
                    onEvent = { uri -> onEvent(AnalyzerEvent.OnClothesCaptured(uri)) },
                    navTo = { navTo(KoColorRoute.Camera("clothes")) }
                )
            }
        }

        OccasionFilter(
            uiState = uiState.selectedOccasion,
            onEvent = { onEvent(AnalyzerEvent.OnOccasionSelected(it)) },
            navTo = {}
        )

        LocationInput(
            uiState = uiState.locationName to uiState.isLocating,
            onEvent = { event ->
                when (event) {
                    is String? -> onEvent(AnalyzerEvent.OnLocationChanged(event))
                    else -> onEvent(AnalyzerEvent.OnDetectLocationClicked)
                }
            },
            navTo = {}
        )

        Button(
            onClick = { onEvent(AnalyzerEvent.OnAnalyzeClicked) },
            enabled = uiState.faceUri != null || uiState.hairUri != null || uiState.shoesUri != null || uiState.clothesUri != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Analyze My Look")
        }
    }
}

@Composable
fun OccasionFilter(
    uiState: String,
    onEvent: (String) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val occasions = listOf("Work", "Date Night", "Outdoor/Sport", "Formal")
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Select Occasion", style = MaterialTheme.typography.labelMedium)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            occasions.forEach { occasion ->
                FilterChip(
                    selected = uiState == occasion,
                    onClick = { onEvent(occasion) },
                    label = { Text(occasion) }
                )
            }
        }
    }
}

@Composable
fun LocationInput(
    uiState: Pair<String?, Boolean>,
    onEvent: (Any?) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val locationName = uiState.first
    val isLocating = uiState.second

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Style Location", style = MaterialTheme.typography.labelMedium)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = locationName ?: "",
                onValueChange = { onEvent(it.takeIf { it.isNotBlank() }) },
                modifier = Modifier.weight(1f),
                placeholder = { Text("City, Style Capital...") },
                label = { Text("Local Context") },
                singleLine = true,
                trailingIcon = {
                    if (isLocating) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        IconButton(onClick = { onEvent(Unit) }) {
                            Icon(Icons.Default.MyLocation, contentDescription = "Detect Location")
                        }
                    }
                }
            )
        }
        Text(
            "AI will tailor your palette to local fashion trends.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun StyleCaptureSlot(
    uiState: Pair<String, String?>,
    onEvent: (String) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val title = uiState.first
    val uri = uiState.second

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { pickedUri ->
        pickedUri?.let { onEvent(it.toString()) }
    }

    var showOptions by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
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
            title = { Text("Capture $title") },
            text = { Text("Choose a photo source") },
            confirmButton = {
                TextButton(onClick = {
                    showOptions = false
                    navTo(KoColorRoute.Camera(title.lowercase().substringAfter("your ")))
                }) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Camera")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showOptions = false
                    launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gallery")
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
            uiState = "Your Face" to null,
            onEvent = {},
            navTo = {}
        )
    }
}

@Composable
fun AnalysisResultScreen(
    uiState: FashionAdvice,
    onEvent: (AnalyzerEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Style Analysis Result", style = MaterialTheme.typography.headlineSmall)
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Seasonal Type: ${uiState.seasonalType}", style = MaterialTheme.typography.titleMedium)
                    Text("Undertone: ${uiState.undertone}", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(uiState.summary)
                }
            }
        }
        item {
            Text("Recommended Makeup & Nail Palette", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.recommendedPalette.forEach { hex ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(parseColor(hex))
                    )
                }
            }
        }
        item {
            Text("Makeup & Nail Suggestions", style = MaterialTheme.typography.titleMedium)
        }
        items(uiState.makeupSuggestions) { suggestion ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(suggestion.category, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        Text(suggestion.advice)
                    }
                    if (suggestion.category.contains("Nail", ignoreCase = true) || 
                        suggestion.category.contains("Lip", ignoreCase = true) || 
                        suggestion.category.contains("Blush", ignoreCase = true)) {
                        Button(
                            onClick = {
                                val color = suggestion.recommendedColors.firstOrNull() ?: "#FF0000"
                                if (suggestion.category.contains("Nail", ignoreCase = true)) {
                                    val finish = if (suggestion.advice.contains("Matte", ignoreCase = true)) "MATTE"
                                                 else if (suggestion.advice.contains("Metallic", ignoreCase = true)) "METALLIC"
                                                 else "GLOSSY"
                                    navTo(KoColorRoute.NailLab(color, finish))
                                } else {
                                    navTo(KoColorRoute.FaceLab(color, suggestion.category))
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Experience", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(onClick = { onEvent(AnalyzerEvent.OnResetClicked) }, modifier = Modifier.weight(1f)) {
                    Text("Discard")
                }
                Button(onClick = { onEvent(AnalyzerEvent.OnSaveClicked(uiState)) }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Analysis")
                }
            }
        }
    }
}

fun parseColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        Color.Gray
    }
}

@Preview(showBackground = true)
@Composable
private fun AnalyzerScreenPreview_Idle() {
    MaterialTheme {
        AnalyzerScreen(
            uiState = AnalyzerScreenUiState(),
            onEvent = {},
            navTo = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AnalyzerScreenPreview_Partial() {
    MaterialTheme {
        AnalyzerScreen(
            uiState = AnalyzerScreenUiState(faceUri = "content://dummy"),
            onEvent = {},
            navTo = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AnalyzerScreenPreview_Success() {
    MaterialTheme {
        AnalyzerScreen(
            uiState = AnalyzerScreenUiState(
                analyzerState = AnalyzerUiState.Success(
                    FashionAdvice(
                        summary = "Holistic coordination with your outfit and shoes.",
                        seasonalType = SeasonalType.WINTER,
                        undertone = Undertone.COOL,
                        makeupSuggestions = listOf(
                            com.zoewave.probase.kocolor.model.MakeupSuggestion("Lip", "Berry red", listOf("#800020")),
                            com.zoewave.probase.kocolor.model.MakeupSuggestion("Nails", "Deep plum gloss", listOf("#4B0082"))
                        ),
                        outfitSuggestions = emptyList(),
                        recommendedPalette = listOf("#800020", "#C0C0C0", "#000080")
                    )
                )
            ),
            onEvent = {},
            navTo = {}
        )
    }
}
