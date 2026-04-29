package com.zoewave.probase.gotmind.mobile.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zoewave.probase.gotmind.mobile.ui.GameViewModel

@Composable
fun GameScreen(viewModel: GameViewModel) {
    val gameState by viewModel.gameState.collectAsState()
    val topScores by viewModel.topScores.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (gameState.isGameOver) {
            Text(text = "Game Over!", style = MaterialTheme.typography.headlineLarge)
            Text(text = "Score: ${gameState.currentScore}")
            Button(onClick = { viewModel.resetGame() }) {
                Text("Restart")
            }
        } else {
            Text(text = "GotMind Game", style = MaterialTheme.typography.headlineMedium)
            Text(text = "Current Score: ${gameState.currentScore}")
            Button(onClick = { viewModel.onScoreUpdate(10) }, modifier = Modifier.padding(top = 16.dp)) {
                Text("Tap to Score!")
            }
            Button(onClick = { viewModel.onGameOver() }, modifier = Modifier.padding(top = 8.dp)) {
                Text("End Game")
            }
        }

        Text(
            text = "High Scores:",
            modifier = Modifier.padding(top = 32.dp),
            style = MaterialTheme.typography.titleMedium
        )
        topScores.forEach { score ->
            Text(text = "${score.value}")
        }
    }
}
