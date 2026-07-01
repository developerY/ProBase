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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.gotmind.mobile.R
import com.zoewave.probase.gotmind.model.GotMindRoute

@Composable
fun GameScreen(
    uiState: GotMindClassicUiState,
    modifier: Modifier = Modifier,
    onEvent: (GotMindClassicEvent) -> Unit,
    navTo: (GotMindRoute) -> Unit
) {
    val gameState = uiState.game
    val topScores = uiState.topScores

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (gameState.isGameOver) {
            Text(
                text = stringResource(R.string.classic_game_over), 
                style = MaterialTheme.typography.headlineLarge
            )
            Text(text = stringResource(R.string.classic_score_label, gameState.currentScore))
            Button(onClick = { onEvent(GotMindClassicEvent.ResetGame) }) {
                Text(stringResource(R.string.classic_restart))
            }
        } else {
            Text(
                text = stringResource(R.string.classic_title), 
                style = MaterialTheme.typography.headlineMedium
            )
            Text(text = stringResource(R.string.classic_current_score, gameState.currentScore))
            Button(onClick = { onEvent(GotMindClassicEvent.ScoreUpdate(10)) }, modifier = Modifier.padding(top = 16.dp)) {
                Text(stringResource(R.string.classic_tap_to_score))
            }
            Button(onClick = { onEvent(GotMindClassicEvent.GameOver) }, modifier = Modifier.padding(top = 8.dp)) {
                Text(stringResource(R.string.classic_end_game))
            }
        }

        Text(
            text = stringResource(R.string.classic_high_scores),
            modifier = Modifier.padding(top = 32.dp),
            style = MaterialTheme.typography.titleMedium
        )
        topScores.forEach { score ->
            Text(text = "${score.value}")
        }
    }
}

@Preview
@Composable
private fun GameScreenPreview() {
    MaterialTheme {
        GameScreen(
            uiState = GotMindClassicUiState(),
            onEvent = {},
            navTo = {}
        )
    }
}
