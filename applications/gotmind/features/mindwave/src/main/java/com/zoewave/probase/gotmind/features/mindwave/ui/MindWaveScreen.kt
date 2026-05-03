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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.gotmind.features.mindwave.HapticSignal
import com.zoewave.probase.gotmind.features.mindwave.MindWaveEvent
import com.zoewave.probase.gotmind.features.mindwave.MindWaveState
import com.zoewave.probase.gotmind.features.mindwave.Node
import kotlin.random.Random

@Composable
fun MindWaveScreen(
    uiState: MindWaveState,
    onNav: (String) -> Unit,
    onEvent: (MindWaveEvent) -> Unit
) {
    var showControls by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

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
                .padding(24.dp),
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

            Text(
                text = if (uiState.isPlayingSequence) "Watch the Wave..." else "Repeat the Pattern",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = if (uiState.isPlayingSequence) MaterialTheme.colorScheme.secondary else Color.White
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Game Grid (4x4)
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
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(if (uiState.isGameOver) "TRY AGAIN" else "START GAME", fontWeight = FontWeight.Black)
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
fun MindWaveNode(
    node: Node,
    isClickable: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (node.isFlashing) 1.2f else 1.0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessLow),
        label = "Pulse"
    )

    val color by animateColorAsState(
        targetValue = when {
            node.isFlashing -> Color(0xFF00E5FF)
            else -> Color.White.copy(alpha = 0.1f)
        },
        animationSpec = tween(if (node.isFlashing) 100 else 300),
        label = "Color"
    )

    Box(
        modifier = Modifier
            .size(64.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(color)
            .border(2.dp, if (node.isFlashing) Color(0xFF00E5FF).copy(alpha = 0.5f) else Color.Transparent, CircleShape)
            .clickable(enabled = isClickable) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // Futuristic ripple inside
        if (node.isFlashing) {
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
