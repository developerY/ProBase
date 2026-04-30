package com.zoewave.probase.gotmind.features.memblox.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.gotmind.features.memblox.MemBloxState
import com.zoewave.probase.gotmind.features.memblox.MemBloxViewModel
import com.zoewave.probase.gotmind.features.memblox.PowerUpType
import com.zoewave.probase.gotmind.model.memblox.MemBloxBlock
import com.zoewave.probase.gotmind.model.memblox.MemBloxDifficulty
import java.util.Locale

@Composable
fun MemBloxScreen(viewModel: MemBloxViewModel) {
    val state by viewModel.uiState.collectAsState()
    var showAnalytics by remember { mutableStateOf(false) }

    if (!state.isStarted) {
        DifficultySelectionScreen(onDifficultySelected = { viewModel.startGame(it) })
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F)) // Deep dark background
    ) {
        // --- Sleek Header ---
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(12.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = Color(0xFF1E1E1E)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Score",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = state.score.toString(),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Box(contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Pairs",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "${state.pairsMatched}/${state.targetPairs}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    IconButton(
                        onClick = { showAnalytics = !showAnalytics },
                        modifier = Modifier.background(
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                            CircleShape
                        )
                    ) {
                        Icon(
                            if (showAnalytics) Icons.Default.KeyboardArrowUp else Icons.Default.Analytics,
                            contentDescription = "Analytics",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Progress Bar to Victory
                val progress by animateFloatAsState(
                    targetValue = state.pairsMatched.toFloat() / state.targetPairs,
                    animationSpec = tween(500)
                )
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    strokeCap = StrokeCap.Round
                )

                if (state.combo > 1) {
                    Text(
                        text = "COMBO X${String.format(Locale.getDefault(), "%.1f", state.multiplier)} 🔥",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFFF5722),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                AnimatedVisibility(visible = showAnalytics) {
                    Column(
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .fillMaxWidth()
                    ) {
                        val loadPct = if (state.cols * state.rows > 0) (state.peakBoardBlocks * 100 / (state.cols * state.rows)) else 0
                        val avgMatchSec = state.avgMatchTimeMs / 1000f

                        AnalyticsRow("Hit Rate", "${(state.matchAccuracy * 100).toInt()}%")
                        AnalyticsRow("Best Streak", "${state.bestMatchStreak}")
                        AnalyticsRow("Avg Match Time", String.format(Locale.getDefault(), "%.1fs", avgMatchSec))
                        AnalyticsRow("Peak Board Load", "$loadPct%")
                        AnalyticsRow("Solubility", "${(state.successfulMatches * 100 / (state.totalClicks.coerceAtLeast(1)))}%")
                    }
                }
            }
        }

        // --- Power-Up Chips ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PowerUpType.entries.forEach { type ->
                val count = state.powerUps[type] ?: 0
                val isAvailable = count > 0 && !state.isGameOver && !state.isVictory
                
                ElevatedAssistChip(
                    onClick = { viewModel.usePowerUp(type) },
                    label = { 
                        Text(
                            text = "${type.label} ($count)",
                            style = MaterialTheme.typography.labelLarge
                        ) 
                    },
                    leadingIcon = { Text(type.icon) },
                    enabled = isAvailable,
                    shape = RoundedCornerShape(12.dp),
                    colors = androidx.compose.material3.AssistChipDefaults.elevatedAssistChipColors(
                        containerColor = if (isAvailable) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                        labelColor = if (isAvailable) MaterialTheme.colorScheme.onSecondaryContainer else Color.Gray
                    )
                )
            }
        }

        // --- Main Game Board ---
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(8.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1A1A1A), Color(0xFF050505))
                    )
                )
                .border(2.dp, if (state.isFrozen) Color(0xFF03A9F4) else Color(0xFF333333), RoundedCornerShape(16.dp))
        ) {
            val blockSize = maxWidth / state.cols
            val blockHeight = maxHeight / state.rows

            // Render Blocks
            state.grid.forEach { block ->
                val nukingColor = state.nukingBlockIds[block.id]
                MemBloxBlockRender(
                    block = block,
                    blockSize = blockSize,
                    blockHeight = blockHeight,
                    isRevealed = state.isRevealed,
                    nukingColor = nukingColor?.let { Color(it) },
                    onClick = { viewModel.onBlockClick(block) }
                )
            }

            // Freeze Overlay Effect
            if (state.isFrozen) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF03A9F4).copy(alpha = 0.1f))
                )
            }

            // End Game Overlays
            if (state.isGameOver || state.isVictory) {
                EndGameOverlay(
                    state = state,
                    onRetry = { viewModel.startGame(state.difficulty) },
                    onChangeDifficulty = { viewModel.resetToDifficultySelection() }
                )
            }
        }
    }
}

@Composable
fun MemBloxBlockRender(
    block: MemBloxBlock,
    blockSize: Dp,
    blockHeight: Dp,
    isRevealed: Boolean,
    nukingColor: Color?,
    onClick: () -> Unit
) {
    val isFlipped = block.isFlipped || isRevealed || nukingColor != null
    
    val baseColor = if (nukingColor != null) nukingColor else Color(block.color)
    val displayColor by animateColorAsState(
        targetValue = if (isFlipped && nukingColor == null) Color.White else baseColor,
        animationSpec = tween(300)
    )

    Box(
        modifier = Modifier
            .size(blockSize, blockHeight)
            .offset(x = blockSize * block.col, y = blockHeight * block.row)
            .padding(1.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(
                if (isFlipped && nukingColor == null) {
                    Brush.linearGradient(listOf(Color.White, Color(0xFFF5F5F5)))
                } else {
                    Brush.linearGradient(
                        colors = listOf(
                            displayColor.copy(alpha = 0.9f),
                            displayColor.darken(0.2f)
                        )
                    )
                }
            )
            .drawWithContent {
                drawContent()
                // Gloss effect
                drawRect(
                    brush = Brush.linearGradient(
                        0.0f to Color.White.copy(alpha = 0.2f),
                        0.5f to Color.Transparent,
                        1.0f to Color.Transparent
                    )
                )
            }
            .border(0.5.dp, Color.Black.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isFlipped) {
            Text(
                text = block.emoji, 
                fontSize = (blockSize.value * 0.65).sp,
                modifier = Modifier.scale(animateFloatAsState(1f).value)
            )
        }
    }
}

@Composable
fun EndGameOverlay(
    state: MemBloxState,
    onRetry: () -> Unit,
    onChangeDifficulty: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Surface(
                color = if (state.isVictory) Color(0xFF4CAF50) else Color(0xFFF44336),
                shape = CircleShape,
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (state.isVictory) "🏆" else "💀",
                        fontSize = 40.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (state.isVictory) "VICTORY!" else "GAME OVER",
                color = if (state.isVictory) Color.Green else Color.Red,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    EndGameStat("Final Score", state.score.toString())
                    EndGameStat("Best Streak", state.bestMatchStreak.toString())
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    EndGameStat("Hit Rate", "${(state.matchAccuracy * 100).toInt()}%")
                    EndGameStat("Avg Time", String.format(Locale.getDefault(), "%.1fs", state.avgMatchTimeMs / 1000f))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onRetry,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Retry Level", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onChangeDifficulty,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Change Difficulty", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun EndGameStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = Color.Gray, style = MaterialTheme.typography.labelMedium)
        Text(text = value, color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
    }
}

@Composable
fun AnalyticsRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun DifficultySelectionScreen(onDifficultySelected: (MemBloxDifficulty) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "MemBlox",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Black,
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Master your memory with gravity",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        MemBloxDifficulty.entries.forEach { difficulty ->
            DifficultyCard(difficulty, onDifficultySelected)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun DifficultyCard(difficulty: MemBloxDifficulty, onSelected: (MemBloxDifficulty) -> Unit) {
    val color = when (difficulty) {
        MemBloxDifficulty.EASY -> Color(0xFF4CAF50)
        MemBloxDifficulty.MEDIUM -> Color(0xFFFFC107)
        MemBloxDifficulty.EXPERT -> Color(0xFFF44336)
    }

    ElevatedCard(
        onClick = { onSelected(difficulty) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (difficulty) {
                        MemBloxDifficulty.EASY -> "🌱"
                        MemBloxDifficulty.MEDIUM -> "⚡"
                        MemBloxDifficulty.EXPERT -> "🔥"
                    },
                    fontSize = 24.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = difficulty.label,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "${difficulty.cols}x${difficulty.rows} Board • ${difficulty.targetPairs} Pairs",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            Icon(
                Icons.Default.Info,
                contentDescription = null,
                tint = Color.Gray.copy(alpha = 0.5f)
            )
        }
    }
}

// Extension to darken color
fun Color.darken(factor: Float): Color {
    return Color(
        red = (red * (1 - factor)).coerceIn(0f, 1f),
        green = (green * (1 - factor)).coerceIn(0f, 1f),
        blue = (blue * (1 - factor)).coerceIn(0f, 1f),
        alpha = alpha
    )
}
