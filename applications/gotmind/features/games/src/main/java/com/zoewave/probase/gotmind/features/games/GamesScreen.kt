package com.zoewave.probase.gotmind.features.games

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun GamesScreen(
    onLaunchGotMindClassic: () -> Unit,
    onLaunchMemBlox: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "GotMind Games",
            style = MaterialTheme.typography.displayMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Button(
            onClick = onLaunchGotMindClassic,
            modifier = Modifier.padding(8.dp)
        ) {
            Text("GotMind Classic")
        }

        Button(
            onClick = onLaunchMemBlox,
            modifier = Modifier.padding(8.dp)
        ) {
            Text("MemBlox")
        }
    }
}
