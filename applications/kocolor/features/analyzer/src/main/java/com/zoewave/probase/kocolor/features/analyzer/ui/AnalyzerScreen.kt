package com.zoewave.probase.kocolor.features.analyzer.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    onBack: () -> Unit,
    onNavigateToCamera: (target: String) -> Unit,
    onAnalysisSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AnalyzerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AnalyzerScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        navTo = { route ->
            when (route) {
                null -> onBack()
                is KoColorRoute.Camera -> onNavigateToCamera(route.target)
                else -> { /* Handle other routes if needed */ }
            }
        },
        onAnalysisSaved = onAnalysisSaved,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyzerScreen(
    uiState: AnalyzerScreenUiState,
    onEvent: (AnalyzerEvent) -> Unit,
    navTo: (KoColorRoute?) -> Unit,
    onAnalysisSaved: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fashion Analyzer") },
                navigationIcon = {
                    IconButton(onClick = { navTo(null) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState.analyzerState) {
                is AnalyzerUiState.Idle -> {
                    StyleCaptureState(
                        uiState = uiState,
                        onEvent = onEvent,
                        onCaptureCamera = { target -> navTo(KoColorRoute.Camera(target)) },
                        onPhotoPicked = { target, uri ->
                            when (target) {
                                "face" -> onEvent(AnalyzerEvent.OnFaceCaptured(uri))
                                "hair" -> onEvent(AnalyzerEvent.OnHairCaptured(uri))
                                "shoes" -> onEvent(AnalyzerEvent.OnShoesCaptured(uri))
                                "clothes" -> onEvent(AnalyzerEvent.OnClothesCaptured(uri))
                            }
                        },
                        onAnalyze = { onEvent(AnalyzerEvent.OnAnalyzeClicked) }
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
                        advice = state.advice,
                        onSave = {
                            onEvent(AnalyzerEvent.OnSaveClicked(state.advice))
                            onAnalysisSaved()
                        },
                        onReset = { onEvent(AnalyzerEvent.OnResetClicked) }
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
    onCaptureCamera: (String) -> Unit,
    onPhotoPicked: (String, String) -> Unit,
    onAnalyze: () -> Unit
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
                    title = "Your Face",
                    uri = uiState.faceUri,
                    onCamera = { onCaptureCamera("face") },
                    onGallery = { uri -> onPhotoPicked("face", uri) },
                    modifier = Modifier.weight(1f)
                )
                StyleCaptureSlot(
                    title = "Your Hair",
                    uri = uiState.hairUri,
                    onCamera = { onCaptureCamera("hair") },
                    onGallery = { uri -> onPhotoPicked("hair", uri) },
                    modifier = Modifier.weight(1f)
                )
            }
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StyleCaptureSlot(
                    title = "Your Shoes",
                    uri = uiState.shoesUri,
                    onCamera = { onCaptureCamera("shoes") },
                    onGallery = { uri -> onPhotoPicked("shoes", uri) },
                    modifier = Modifier.weight(1f)
                )
                StyleCaptureSlot(
                    title = "Your Clothes",
                    uri = uiState.clothesUri,
                    onCamera = { onCaptureCamera("clothes") },
                    onGallery = { uri -> onPhotoPicked("clothes", uri) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        OccasionFilter(
            selectedOccasion = uiState.selectedOccasion,
            onOccasionSelected = { onEvent(AnalyzerEvent.OnOccasionSelected(it)) }
        )

        Button(
            onClick = onAnalyze,
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
    selectedOccasion: String,
    onOccasionSelected: (String) -> Unit
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
                    selected = selectedOccasion == occasion,
                    onClick = { onOccasionSelected(occasion) },
                    label = { Text(occasion) }
                )
            }
        }
    }
}

@Composable
fun StyleCaptureSlot(
    title: String,
    uri: String?,
    onCamera: () -> Unit,
    onGallery: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { pickedUri ->
        pickedUri?.let { onGallery(it.toString()) }
    }

    var showOptions by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxHeight()
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
                    onCamera()
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

@Composable
fun AnalysisResultScreen(advice: FashionAdvice, onSave: () -> Unit, onReset: () -> Unit) {
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
                    Text("Seasonal Type: ${advice.seasonalType}", style = MaterialTheme.typography.titleMedium)
                    Text("Undertone: ${advice.undertone}", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(advice.summary)
                }
            }
        }
        item {
            Text("Recommended Makeup & Nail Palette", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                advice.recommendedPalette.forEach { hex ->
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
        items(advice.makeupSuggestions) { suggestion ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(suggestion.category, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    Text(suggestion.advice)
                }
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(onClick = onReset, modifier = Modifier.weight(1f)) {
                    Text("Discard")
                }
                Button(onClick = onSave, modifier = Modifier.weight(1f)) {
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
            navTo = {},
            onAnalysisSaved = {}
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
            navTo = {},
            onAnalysisSaved = {}
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
            navTo = {},
            onAnalysisSaved = {}
        )
    }
}
