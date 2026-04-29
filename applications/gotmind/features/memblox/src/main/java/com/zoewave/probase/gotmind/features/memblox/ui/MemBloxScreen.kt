package com.zoewave.probase.gotmind.features.memblox.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.gotmind.features.memblox.MemBloxViewModel

@Composable
fun MemBloxScreen(viewModel: MemBloxViewModel) {
    val state by viewModel.uiState.collectAsState()

    // Calculate stats
    val emojiCounts = state.grid.groupBy { it.emoji }.mapValues { it.value.size }
    val matchableBlocks = state.grid.count { (emojiCounts[it.emoji] ?: 0) >= 2 }
    val matchProbability = if (state.grid.isEmpty()) 0 else (matchableBlocks * 100 / state.grid.size)
    val activePairs = state.grid.groupBy { it.emoji }.count { it.value.size >= 2 }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header Stats
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Score: ${state.score}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Pairs: ${state.pairsMatched} / ${state.targetPairs}",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Active Pairs on Board: $activePairs",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Match Solubility: $matchProbability%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (matchProbability < 30) Color.Red else Color.Unspecified
                )
            }
        }

        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(4.dp)
                .background(Color(0xFF1A1A1A))
                .border(2.dp, Color.Black)
        ) {
            val blockSize = maxWidth / 12
            val blockHeight = maxHeight / 20

            state.grid.forEach { block ->
                Box(
                    modifier = Modifier
                        .size(blockSize, blockHeight)
                        .offset(x = blockSize * block.col, y = blockHeight * block.row)
                        .background(if (block.isFlipped) Color.White else Color(block.color))
                        .border(1.dp, Color.Black.copy(alpha = 0.2f))
                        .clickable { viewModel.onBlockClick(block) },
                    contentAlignment = Alignment.Center
                ) {
                    if (block.isFlipped) {
                        Text(text = block.emoji, fontSize = 18.sp)
                    }
                }
            }

            // Game Over / Victory Overlays
            if (state.isGameOver || state.isVictory) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.8f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (state.isVictory) "VICTORY!" else "GAME OVER",
                            color = if (state.isVictory) Color.Green else Color.Red,
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Final Score: ${state.score}",
                            color = Color.White,
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                        Button(onClick = { viewModel.startGame() }) {
                            Text("Play Again")
                        }
                    }
                }
            }
        }
    }
}
