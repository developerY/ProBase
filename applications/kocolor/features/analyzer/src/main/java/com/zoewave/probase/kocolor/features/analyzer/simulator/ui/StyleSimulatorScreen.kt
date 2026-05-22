package com.zoewave.probase.kocolor.features.analyzer.simulator.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zoewave.probase.kocolor.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StyleSimulatorScreen(
    uiState: StyleSimulatorUiState,
    onEvent: (SimulatorEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("STYLE SIMULATOR", style = MaterialTheme.typography.labelLarge, letterSpacing = 4.sp) },
                navigationIcon = {
                    IconButton(onClick = { navTo(KoColorRoute.Home) }) {
                        Icon(Icons.Default.Close, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            MagicBackground()

            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                AnimatedContent(
                    targetState = uiState.simulationStep,
                    transitionSpec = { fadeIn(tween(1000)) togetherWith fadeOut(tween(1000)) },
                    label = "step_transition"
                ) { step ->
                    when (step) {
                        SimulationStep.MESSAGING -> MessagingStep(
                            uiState = uiState.userMessage,
                            onEvent = onEvent,
                            navTo = navTo
                        )
                        SimulationStep.BIO_MARKERS, SimulationStep.ROUTINE, SimulationStep.GENERATING -> AnalysisStep(
                            uiState = uiState,
                            onEvent = onEvent,
                            navTo = navTo
                        )
                        SimulationStep.RESULT -> ResultStep(
                            uiState = uiState,
                            onEvent = onEvent,
                            navTo = navTo
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MagicBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "magic")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(10000, easing = LinearEasing), RepeatMode.Reverse),
        label = "phase"
    )

    Box(modifier = Modifier.fillMaxSize().alpha(0.1f).blur(100.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF6200EE), Color.Transparent),
                    center = center.copy(x = center.x * phase * 2, y = center.y * (1 - phase) * 2)
                ),
                radius = size.maxDimension
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MessagingStepPreview() {
    MaterialTheme {
        MessagingStep(uiState = "High-stakes negotiation.", onEvent = {}, navTo = {})
    }
}

@Composable
fun MessagingStep(
    uiState: String,
    onEvent: (SimulatorEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(32.dp)) {
        Spacer(Modifier.height(40.dp))
        Text(
            text = "Define Your Intent.",
            style = MaterialTheme.typography.displayMedium,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Light,
            lineHeight = 52.sp
        )
        
        OutlinedTextField(
            value = uiState,
            onValueChange = { onEvent(SimulatorEvent.UpdateMessage(it)) },
            placeholder = { Text("e.g. A crisp look for high-stakes negotiation.", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.alpha(0.4f)) },
            modifier = Modifier.fillMaxWidth().height(200.dp),
            shape = RoundedCornerShape(32.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                focusedBorderColor = MaterialTheme.colorScheme.primary
            )
        )

        Button(
            onClick = { onEvent(SimulatorEvent.StartSimulation) },
            modifier = Modifier.fillMaxWidth().height(80.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Text("Begin Simulation", style = MaterialTheme.typography.titleMedium, letterSpacing = 2.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AnalysisStepPreview() {
    MaterialTheme {
        AnalysisStep(uiState = StyleSimulatorUiState(simulationStep = SimulationStep.BIO_MARKERS), onEvent = {}, navTo = {})
    }
}

@Composable
fun AnalysisStep(
    uiState: StyleSimulatorUiState,
    onEvent: (SimulatorEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val scale by infiniteTransition.animateFloat(
            initialValue = 0.9f, targetValue = 1.1f,
            animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse),
            label = "scale"
        )

        Box(
            modifier = Modifier.size(200.dp).graphicsLayer(scaleX = scale, scaleY = scale),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.fillMaxSize(),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            )
            Icon(
                imageVector = when (uiState.simulationStep) {
                    SimulationStep.BIO_MARKERS -> Icons.Default.Favorite
                    SimulationStep.ROUTINE -> Icons.Default.AutoAwesome
                    else -> Icons.Default.Grain
                },
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(Modifier.height(48.dp))
        
        Text(
            text = when (uiState.simulationStep) {
                SimulationStep.BIO_MARKERS -> "Reading Bio-Markers..."
                SimulationStep.ROUTINE -> "Syncing Daily Rituals..."
                SimulationStep.GENERATING -> "Architecting Style..."
                else -> "Magic..."
            },
            style = MaterialTheme.typography.headlineSmall,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(Modifier.height(16.dp))
        
        Text(
            text = "Synthesizing your physical context with your stated intent.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(280.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ResultStepPreview() {
    MaterialTheme {
        ResultStep(uiState = StyleSimulatorUiState(), onEvent = {}, navTo = {})
    }
}

@Composable
fun ResultStep(
    uiState: StyleSimulatorUiState,
    onEvent: (SimulatorEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(40.dp)
    ) {
        item {
            Column {
                Text("Your Blueprint.", style = MaterialTheme.typography.displaySmall, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(if (uiState.isLocalResult) Color.Gray else Color.Green))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (uiState.isLocalResult) "BEST-EFFORT LOCAL CALCULATION" else "OPTIMIZED BY GEMINI AI", 
                        style = MaterialTheme.typography.labelSmall, 
                        fontWeight = FontWeight.Black, 
                        letterSpacing = 1.sp
                    )
                }
                
                uiState.rationale?.let {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        lineHeight = 22.sp
                    )
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Text("CHROMATIC CORE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    uiState.recommendedPalette.forEach { hex ->
                        Box(
                            modifier = Modifier.size(72.dp).clip(RoundedCornerShape(20.dp)).background(parseColor(hex))
                        )
                    }
                }
            }
        }

        item {
            Text("VAULT SELECTIONS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }

        items(uiState.recommendedClothing) { item ->
            ResultCard(uiState = item, onEvent = {}, navTo = {})
        }
        
        item {
            Button(
                onClick = { onEvent(SimulatorEvent.SaveToPalette) },
                modifier = Modifier.fillMaxWidth().height(80.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("Lock Selection to Palette", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ResultCardPreview() {
    MaterialTheme {
        ResultCard(
            uiState = ClothingItem(name = "Shirt", category = ClothingCategory.TOPS),
            onEvent = {},
            navTo = {}
        )
    }
}

@Composable
fun ResultCard(
    uiState: ClothingItem,
    onEvent: (Unit) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val item = uiState
    Card(
        modifier = Modifier.fillMaxWidth().height(160.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.fillMaxHeight().width(140.dp).background(item.colorHex?.let { parseColor(it) } ?: Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                if (item.imageUrl != null) {
                    AsyncImage(model = item.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Icon(Icons.Default.Inventory2, null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(40.dp))
                }
            }
            Column(modifier = Modifier.padding(24.dp)) {
                Text(text = item.category.name, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text(text = item.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
                Text(text = item.brand ?: "Bespoke", style = MaterialTheme.typography.bodySmall, modifier = Modifier.alpha(0.6f))
            }
        }
    }
}

private fun parseColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        Color.Gray
    }
}

@Preview(showBackground = true)
@Composable
private fun StyleSimulatorScreenPreview() {
    StyleSimulatorScreen(
        uiState = StyleSimulatorUiState(
            simulationStep = SimulationStep.RESULT,
            recommendedPalette = listOf("#F4D03F", "#16A085", "#2C3E50"),
            recommendedClothing = listOf(ClothingItem(name = "Silk Evening Shirt", category = com.zoewave.probase.kocolor.model.ClothingCategory.TOPS, brand = "ZoeWave"))
        ),
        onEvent = {},
        navTo = {}
    )
}
