package com.zoewave.probase.gotmind.features.memblox.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.gotmind.features.memblox.MemBloxViewModel
import com.zoewave.probase.gotmind.features.memblox.PowerUpType
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

    Column(modifier = Modifier.fillMaxSize()) {
        // Header with Basic Stats & Analytics Toggle
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .statusBarsPadding()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Score: ${state.score}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (state.combo > 1) {
                        Text(
                            text = "Combo x${state.multiplier} 🔥",
                            color = Color(0xFFE91E63),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Pairs: ${state.pairsMatched}/${state.targetPairs}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    IconButton(onClick = { showAnalytics = !showAnalytics }) {
                        Icon(Icons.Default.Info, contentDescription = "Analytics")
                    }
                }
            }

            AnimatedVisibility(visible = showAnalytics) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    val loadPct = if (state.cols * state.rows > 0) (state.peakBoardBlocks * 100 / (state.cols * state.rows)) else 0
                    val avgMatchSec = state.avgMatchTimeMs / 1000f
                    
                    AnalyticsRow("Hit Rate", "${(state.matchAccuracy * 100).toInt()}%")
                    AnalyticsRow("Best Streak", "${state.bestMatchStreak}")
                    AnalyticsRow("Avg Match Time", String.format(Locale.getDefault(), "%.1fs", avgMatchSec))
                    AnalyticsRow("Peak Board Load", "$loadPct%")
                    AnalyticsRow("Efficiency", "${if (state.totalClicks > 0) String.format(Locale.getDefault(), "%.2f", state.score.toFloat() / state.totalClicks) else "0"} pts/click")
                }
            }
        }

        // Power-Up Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            PowerUpType.entries.forEach { type ->
                val count = state.powerUps[type] ?: 0
                Button(
                    onClick = { viewModel.usePowerUp(type) },
                    enabled = count > 0 && !state.isGameOver && !state.isVictory,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (count > 0) MaterialTheme.colorScheme.secondaryContainer else Color.Gray
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Text("${type.icon} ${type.label} ($count)")
                }
            }
        }

        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(4.dp)
                .background(if (state.isFrozen) Color(0xFFE1F5FE) else Color(0xFF1A1A1A))
                .border(2.dp, if (state.isFrozen) Color(0xFF03A9F4) else Color.Black)
        ) {
            val blockSize = maxWidth / state.cols
            val blockHeight = maxHeight / state.rows

            state.grid.forEach { block ->
                val nukingColor = state.nukingBlockIds[block.id]
                Box(
                    modifier = Modifier
                        .size(blockSize, blockHeight)
                        .offset(x = blockSize * block.col, y = blockHeight * block.row)
                        .background(
                            when {
                                nukingColor != null -> Color(nukingColor)
                                block.isFlipped -> Color.White
                                else -> Color(block.color)
                            }
                        )
                        .border(1.dp, Color.Black.copy(alpha = 0.2f))
                        .clickable { viewModel.onBlockClick(block) },
                    contentAlignment = Alignment.Center
                ) {
                    if (block.isFlipped || state.isRevealed || nukingColor != null) {
                        Text(text = block.emoji, fontSize = (blockSize.value * 0.6).sp)
                    }
                }
            }

            // Game Over / Victory Overlays
            if (state.isGameOver || state.isVictory) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.85f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = if (state.isVictory) "VICTORY!" else "GAME OVER",
                            color = if (state.isVictory) Color.Green else Color.Red,
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Black
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text("Final Score: ${state.score}", color = Color.White, style = MaterialTheme.typography.headlineMedium)
                        
                        Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                            EndGameStat("Hit Rate", "${(state.matchAccuracy * 100).toInt()}%")
                            EndGameStat("Best Streak", "${state.bestMatchStreak}")
                        }
                        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                            EndGameStat("Avg Time", String.format(Locale.getDefault(), "%.1fs", state.avgMatchTimeMs / 1000f))
                            EndGameStat("Peak Load", "${(state.peakBoardBlocks * 100 / (state.cols * state.rows))}%")
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Button(
                            onClick = { viewModel.startGame(state.difficulty) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Retry Level")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.resetToDifficultySelection() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Change Difficulty")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EndGameStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
        Text(text = value, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun AnalyticsRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DifficultySelectionScreen(onDifficultySelected: (MemBloxDifficulty) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "MemBlox",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Matching blocks with gravity",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(bottom = 48.dp)
        )
        
        MemBloxDifficulty.entries.forEach { difficulty ->
            Button(
                onClick = { onDifficultySelected(difficulty) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(text = difficulty.label, style = MaterialTheme.typography.headlineSmall)
                    Text(
                        text = "${difficulty.cols}x${difficulty.rows} • ${difficulty.targetPairs} pairs",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
