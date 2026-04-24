package com.zoewave.probase.kocolor.features.analyzer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.zoewave.probase.kocolor.model.FashionAdvice
import com.zoewave.probase.kocolor.model.KoColorRoute
import com.zoewave.probase.kocolor.model.SeasonalType
import com.zoewave.probase.kocolor.model.Undertone

@Composable
fun AnalyzerUiRoute(
    onBack: () -> Unit,
    onNavigateToCamera: () -> Unit,
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
                is KoColorRoute.Camera -> onNavigateToCamera()
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
                    if (uiState.capturedUri == null) {
                        EmptyAnalyzerState(onStartCamera = { navTo(KoColorRoute.Camera("analyzer")) })
                    } else {
                        ReadyToAnalyzeState(
                            uri = uiState.capturedUri,
                            onAnalyze = { onEvent(AnalyzerEvent.OnAnalyzeClicked) },
                            onRetake = { navTo(KoColorRoute.Camera("analyzer")) }
                        )
                    }
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
fun EmptyAnalyzerState(onStartCamera: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            "Take a selfie to find your seasonal color",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 16.dp)
        )
        Button(onClick = onStartCamera, modifier = Modifier.padding(top = 24.dp)) {
            Text("Open Camera")
        }
    }
}

@Composable
fun ReadyToAnalyzeState(uri: String, onAnalyze: () -> Unit, onRetake: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(modifier = Modifier.weight(1f).fillMaxWidth()) {
            AsyncImage(
                model = uri,
                contentDescription = "Captured Selfie",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(onClick = onRetake, modifier = Modifier.weight(1f)) {
                Text("Retake")
            }
            Button(onClick = onAnalyze, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Analyze")
            }
        }
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
            Text("Analysis Result", style = MaterialTheme.typography.headlineSmall)
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
            Text("Recommended Palette", style = MaterialTheme.typography.titleMedium)
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
            Text("Makeup Suggestions", style = MaterialTheme.typography.titleMedium)
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
                    Text("Save Profile")
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
private fun AnalyzerScreenPreview_Loading() {
    MaterialTheme {
        AnalyzerScreen(
            uiState = AnalyzerScreenUiState(analyzerState = AnalyzerUiState.Loading()),
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
                        summary = "You have a vibrant winter look.",
                        seasonalType = SeasonalType.WINTER,
                        undertone = Undertone.COOL,
                        makeupSuggestions = emptyList(),
                        outfitSuggestions = emptyList(),
                        recommendedPalette = listOf("#FF0000", "#00FF00", "#0000FF")
                    )
                )
            ),
            onEvent = {},
            navTo = {},
            onAnalysisSaved = {}
        )
    }
}
