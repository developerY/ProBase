package com.zoewave.probase.gotmind.features.memblox.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.gotmind.features.memblox.MemBloxViewModel

@Composable
fun MemBloxScreen(viewModel: MemBloxViewModel) {
    val state by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "MemBlox Score: ${state.score}",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )

        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(8.dp)
                .background(Color.DarkGray)
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
                        .border(1.dp, Color.Black.copy(alpha = 0.3f))
                        .clickable { viewModel.onBlockClick(block) },
                    contentAlignment = Alignment.Center
                ) {
                    if (block.isFlipped) {
                        Text(text = block.emoji, fontSize = 20.sp)
                    }
                }
            }

            if (state.isGameOver) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "GAME OVER",
                        color = Color.Red,
                        style = MaterialTheme.typography.displayLarge
                    )
                }
            }
        }
    }
}
