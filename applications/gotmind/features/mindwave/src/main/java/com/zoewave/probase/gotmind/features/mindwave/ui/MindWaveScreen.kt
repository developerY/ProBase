package com.zoewave.probase.gotmind.features.mindwave.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.gotmind.features.mindwave.HapticSignal
import com.zoewave.probase.gotmind.features.mindwave.MindWaveEvent
import com.zoewave.probase.gotmind.features.mindwave.MindWaveState
import com.zoewave.probase.gotmind.features.mindwave.Node
import com.zoewave.probase.gotmind.model.MindWaveMode
import kotlin.random.Random

@Composable
fun MindWaveScreen(
    uiState: MindWaveState,
    onNav: (String) -> Unit,
    onEvent: (MindWaveEvent) -> Unit
) {
    var showControls by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    
    val startButtonInteractionSource = remember { MutableInteractionSource() }
    val startButtonPressed by startButtonInteractionSource.collectIsPressedAsState()
    val startButtonScale by animateFloatAsState(
        targetValue = if (startButtonPressed) 0.95f else 1.0f,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessMedium),
        label = "StartPress"
    )

    // Haptic Feedback Observer
    LaunchedEffect(uiState.lastHapticSignal) {
        uiState.lastHapticSignal?.let { signal ->
            when (signal) {
                HapticSignal.LIGHT -> haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                HapticSignal.MEDIUM -> haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                HapticSignal.HEAVY -> haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
            onEvent(MindWaveEvent.HapticConsumed)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F0F0F))) {
        MindWaveBackground()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1E1E1E).copy(alpha = 0.7f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { onNav("BACK") },
                    modifier = Modifier.size(36.dp).background(Color.White.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = uiState.score.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                        Text(text = "SCORE", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(24.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = uiState.level.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(text = "LEVEL", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }

                IconButton(
                    onClick = { showControls = !showControls },
                    modifier = Modifier.size(36.dp).background(if (showControls) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(imageVector = Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.White)
                }
            }

            // Controls Overlay
            AnimatedVisibility(visible = showControls) {
                Column(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E1E1E).copy(alpha = 0.95f))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { 
                                onEvent(MindWaveEvent.ResetGame)
                                onNav("BACK") 
                            },
                            modifier = Modifier.weight(1f).height(40.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.2f), contentColor = Color.Red),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("QUIT", style = MaterialTheme.typography.labelLarge)
                        }
                        
                        Button(
                            onClick = { onEvent(MindWaveEvent.TogglePause) },
                            modifier = Modifier.weight(1f).height(40.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (uiState.isPaused) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.1f),
                                contentColor = if (uiState.isPaused) MaterialTheme.colorScheme.primary else Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                        ) {
                            Icon(if (uiState.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (uiState.isPaused) "RESUME" else "PAUSE", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (uiState.mode == MindWaveMode.SYMPHONY || uiState.mode == MindWaveMode.HARMONIC_ARC) {
                MusicalStaff(activeNodeId = uiState.activeNodeId)
                Spacer(modifier = Modifier.height(12.dp))
                if (uiState.currentSongTitle != null) {
                    Text(
                        text = "Melody: ${uiState.currentSongTitle}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (uiState.isPlayingSequence) "Listen carefully..." else "Repeat the Melody",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (uiState.isPlayingSequence) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.8f)
                )
            } else {
                Text(
                    text = if (uiState.isPlayingSequence) "Watch the Wave..." else "Repeat the Pattern",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = if (uiState.isPlayingSequence) MaterialTheme.colorScheme.secondary else Color.White
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Game Grid or Arc or Ring
            if (uiState.mode == MindWaveMode.HARMONIC_ARC || uiState.mode == MindWaveMode.HARMONIC_RING) {
                val isFullCircle = uiState.mode == MindWaveMode.HARMONIC_RING
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isFullCircle) 450.dp else 300.dp) 
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isFullCircle) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            MusicalStaff(activeNodeId = uiState.activeNodeId)
                            if (uiState.currentSongTitle != null) {
                                Text(
                                    text = uiState.currentSongTitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    val sweepAngle = if (isFullCircle) 360f else 180f
                    val startAngle = if (isFullCircle) 270f else 180f // Start from top for circle
                    
                    uiState.grid.forEachIndexed { index, node ->
                        val angle = startAngle - (index.toFloat() / uiState.grid.size * sweepAngle)
                        val angleRad = Math.toRadians(angle.toDouble())
                        
                        val radiusPx = if (uiState.nodeShape == com.zoewave.probase.gotmind.model.NodeShape.PIANO_KEY) 180 else 140
                        val x = (Math.cos(angleRad) * radiusPx).dp
                        val y = (Math.sin(angleRad) * -radiusPx).dp
                        
                        Box(
                            modifier = Modifier
                                .offset(x = x, y = if (isFullCircle) y else y + 150.dp) // Align arc relative to center
                                .graphicsLayer {
                                    if (uiState.nodeShape == com.zoewave.probase.gotmind.model.NodeShape.PIANO_KEY) {
                                        rotationZ = angle + 90f // Rotate key to face center
                                    }
                                }
                        ) {
                            if (uiState.nodeShape == com.zoewave.probase.gotmind.model.NodeShape.PIANO_KEY) {
                                PianoKeyNode(
                                    node = node,
                                    isClickable = !uiState.isPlayingSequence && !uiState.isGameOver && uiState.isStarted && !uiState.isPaused,
                                    onClick = { onEvent(MindWaveEvent.NodeClick(index)) }
                                )
                            } else {
                                MindWaveNode(
                                    node = node,
                                    isClickable = !uiState.isPlayingSequence && !uiState.isGameOver && uiState.isStarted && !uiState.isPaused,
                                    onClick = { onEvent(MindWaveEvent.NodeClick(index)) }
                                )
                            }
                        }
                    }
                }
            } else {
                // Standard Grid
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    for (row in 0..3) {
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            for (col in 0..3) {
                                val index = row * 4 + col
                                val node = uiState.grid[index]
                                MindWaveNode(
                                    node = node,
                                    isClickable = !uiState.isPlayingSequence && !uiState.isGameOver && uiState.isStarted && !uiState.isPaused,
                                    onClick = { onEvent(MindWaveEvent.NodeClick(index)) }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Feedback Message
            uiState.feedbackMessage?.let {
                Text(
                    text = it,
                    color = if (uiState.isGameOver) Color.Red else Color.Green,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            if (!uiState.isStarted || uiState.isGameOver) {
                Button(
                    onClick = { onEvent(MindWaveEvent.StartGame) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .scale(startButtonScale),
                    shape = RoundedCornerShape(16.dp),
                    interactionSource = startButtonInteractionSource,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(if (uiState.isGameOver) "TRY AGAIN" else "START GAME", fontWeight = FontWeight.Black)
                }
            }
        }
    }

    if (uiState.isGameOver) {
        AlertDialog(
            onDismissRequest = { /* No-op */ },
            containerColor = Color(0xFF1E1E1E),
            titleContentColor = Color.White,
            textContentColor = Color.Gray,
            title = { Text("Sequence Broken") },
            text = { Text("The melody has drifted away. Keep training your sensory memory!") },
            confirmButton = {
                TextButton(onClick = { onEvent(MindWaveEvent.StartGame) }) {
                    Text("RETRY WAVE", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { onNav("BACK") }) {
                    Text("BACK TO HUB", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun MusicalStaff(activeNodeId: Int?) {
    val lineSpacing = 10.dp
    val staffHeight = lineSpacing * 8
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(staffHeight + 20.dp)
            .padding(horizontal = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerY = height / 2f
            val spacing = lineSpacing.toPx()
            
            // Draw Treble Clef Symbol
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = Color.White.copy(alpha = 0.7f).toArgb()
                    textSize = spacing * 5f
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                }
                drawText("𝄞", 0f, centerY + spacing * 1.5f, paint)
            }

            // Draw 5 staff lines
            for (i in -2..2) {
                val y = centerY + i * spacing
                drawLine(
                    color = Color.White.copy(alpha = 0.25f),
                    start = androidx.compose.ui.geometry.Offset(60f, y),
                    end = androidx.compose.ui.geometry.Offset(width, y),
                    strokeWidth = 2f
                )
            }

            // Note mapping (Treble Clef)
            val noteOffsets = mapOf(
                0 to -2.5f, // D#5
                1 to -1.0f, // D5
                2 to -0.5f, // C#5
                3 to -0.5f, // C5
                4 to 0.0f,  // B4
                5 to 0.5f,  // A#4
                6 to 0.5f,  // A4
                7 to 1.0f,  // G#4
                8 to 1.0f,  // G4
                9 to 1.5f,  // F#4
                10 to 1.5f, // F4
                11 to 2.0f, // E4
                12 to 2.5f, // D#4
                13 to 2.5f, // D4
                14 to 3.0f, // C#4
                15 to 3.0f  // C4
            )

            activeNodeId?.let { id ->
                val offsetMultiplier = noteOffsets[id] ?: 0f
                val noteY = centerY + offsetMultiplier * spacing
                
                // Draw Ledger line for C4
                if (offsetMultiplier >= 3.0f) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.6f),
                        start = androidx.compose.ui.geometry.Offset(width/2f - 25f, centerY + 3f * spacing),
                        end = androidx.compose.ui.geometry.Offset(width/2f + 25f, centerY + 3f * spacing),
                        strokeWidth = 3f
                    )
                }

                // Note Head Glow
                drawCircle(
                    color = Color(0xFF00E5FF).copy(alpha = 0.3f),
                    radius = spacing * 0.6f,
                    center = androidx.compose.ui.geometry.Offset(width / 2f, noteY)
                )

                // Main Note Head
                drawCircle(
                    color = Color.White,
                    radius = spacing * 0.4f,
                    center = androidx.compose.ui.geometry.Offset(width / 2f, noteY)
                )
                
                // Sharp symbol (#)
                val isSharp = id in listOf(0, 2, 5, 7, 9, 12, 14)
                if (isSharp) {
                    drawContext.canvas.nativeCanvas.apply {
                        val p = android.graphics.Paint().apply {
                            color = Color(0xFF00E5FF).toArgb()
                            textSize = spacing * 1.5f
                            typeface = android.graphics.Typeface.DEFAULT_BOLD
                        }
                        drawText("#", width/2f + spacing * 0.6f, noteY + spacing * 0.4f, p)
                    }
                }
            }
        }
    }
}

@Composable
fun MindWaveBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "Nebula")
    val particles = remember { List(20) { RandomParticle() } }

    particles.forEach { p ->
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.1f,
            targetValue = 0.4f,
            animationSpec = infiniteRepeatable(
                animation = tween(p.duration, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "Alpha"
        )
        
        Box(
            modifier = Modifier
                .offset(x = p.startX.dp, y = p.startY.dp)
                .size(p.size.dp)
                .alpha(alpha)
                .background(p.color, CircleShape)
        )
    }
}

@Composable
fun PianoKeyNode(
    node: Node,
    isClickable: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessMedium),
        label = "PressScale"
    )

    val scale by animateFloatAsState(
        targetValue = if (node.isFlashing) 1.1f else 1.0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessLow),
        label = "Pulse"
    )

    val baseColor = if (node.color != null) Color(node.color) else Color.White.copy(alpha = 0.1f)
    val color by animateColorAsState(
        targetValue = when {
            node.isFlashing || isPressed -> if (node.color != null) Color.White else Color(0xFF00E5FF)
            else -> baseColor
        },
        animationSpec = tween(if (node.isFlashing || isPressed) 100 else 300),
        label = "Color"
    )

    // Tapered "Long Triangle" shape - mathematically optimized for a seamless ring
    val keyShape = remember {
        GenericShape { size, _ ->
            // For 16 keys, each occupies 22.5 degrees.
            // Ratio of inner arc to outer arc determines the taper.
            val topWidthFactor = 0.55f 
            val xOffset = (1f - topWidthFactor) / 2f
            moveTo(size.width * xOffset, 0f)
            lineTo(size.width * (1f - xOffset), 0f) 
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
    }

    Box(
        modifier = Modifier
            .size(width = 85.dp, height = 120.dp) // Optimized width for flush fitting
            .scale(scale * pressScale)
            .clip(keyShape)
            .background(color)
            .border(1.dp, if (node.isFlashing || isPressed) Color.White else Color.Black.copy(alpha = 0.15f), keyShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = isClickable
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (node.note != null) {
            Text(
                text = node.note,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black.copy(alpha = 0.7f),
                modifier = Modifier
                    .offset(y = 30.dp) // Positioned for maximum visibility on the wedge
                    .graphicsLayer { rotationZ = -90f }
            )
        }
    }
}

@Composable
fun MindWaveNode(
    node: Node,
    isClickable: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessMedium),
        label = "PressScale"
    )

    val scale by animateFloatAsState(
        targetValue = if (node.isFlashing) 1.2f else 1.0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessLow),
        label = "Pulse"
    )

    val baseColor = if (node.color != null) Color(node.color) else Color.White.copy(alpha = 0.1f)
    
    val color by animateColorAsState(
        targetValue = when {
            node.isFlashing || isPressed -> if (node.color != null) Color.White else Color(0xFF00E5FF)
            else -> baseColor
        },
        animationSpec = tween(if (node.isFlashing || isPressed) 100 else 300),
        label = "Color"
    )

    Box(
        modifier = Modifier
            .size(64.dp)
            .scale(scale * pressScale)
            .clip(CircleShape)
            .background(color)
            .border(2.dp, if (node.isFlashing || isPressed) Color.White.copy(alpha = 0.5f) else Color.Transparent, CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = isClickable
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (node.note != null) {
            Text(
                text = node.note,
                style = MaterialTheme.typography.titleMedium, // Larger font
                fontWeight = FontWeight.ExtraBold, // Bolder
                color = Color.Black.copy(alpha = 0.8f) // Dark text for high contrast on pastels
            )
        }
        
        if (node.isFlashing && node.color == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.3f))
            )
        }
    }
}

data class RandomParticle(
    val startX: Int = Random.nextInt(0, 400),
    val startY: Int = Random.nextInt(0, 800),
    val size: Int = Random.nextInt(50, 150),
    val duration: Int = Random.nextInt(3000, 6000),
    val color: Color = listOf(Color(0xFF00E5FF), Color(0xFFE91E63), Color(0xFF673AB7)).random().copy(alpha = 0.3f)
)
