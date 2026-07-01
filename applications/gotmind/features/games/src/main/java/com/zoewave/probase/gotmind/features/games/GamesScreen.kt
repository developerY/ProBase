package com.zoewave.probase.gotmind.features.games

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.zoewave.probase.gotmind.model.GotMindRoute
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun GamesScreen(
    uiState: GamesUiState,
    modifier: Modifier = Modifier,
    onEvent: (GamesEvent) -> Unit,
    navTo: (GotMindRoute) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Image(
            painter = painterResource(R.drawable.app_logo),
            contentDescription = null,
            modifier = Modifier
                .size(240.dp) // Significantly larger logo
                .padding(vertical = 32.dp)
        )

        Text(
            text = stringResource(R.string.applications_gotmind_features_games_title),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Black,
            color = Color.White,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // --- MemBlox (Active) ---
        Button(
            onClick = { navTo(GotMindRoute.MemBlox) },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = stringResource(R.string.applications_gotmind_features_games_memblox),
                style = MaterialTheme.typography.titleLarge
            )
        }

        // --- SoundMind (Active) ---
        Button(
            onClick = { navTo(GotMindRoute.SoundMind) },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.applications_gotmind_features_games_placeholder_1),
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }

        // --- GotMind Classic (Grayed Out) ---
        Button(
            onClick = { },
            enabled = false,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                disabledContainerColor = Color.White.copy(alpha = 0.1f),
                disabledContentColor = Color.Gray
            )
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.applications_gotmind_features_games_classic),
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }

        // --- Future Placeholder 2 ---
        PlaceholderGameButton(stringResource(R.string.applications_gotmind_features_games_placeholder_2))

        // --- Future Placeholder 3 ---
        PlaceholderGameButton(stringResource(R.string.applications_gotmind_features_games_placeholder_3))
    }
}

@Composable
fun PlaceholderGameButton(title: String) {
    Button(
        onClick = { },
        enabled = false,
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            disabledContainerColor = Color.White.copy(alpha = 0.05f),
            disabledContentColor = Color.DarkGray
        )
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = stringResource(R.string.applications_gotmind_features_games_coming_soon),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview
@Composable
private fun GamesScreenPreview() {
    MaterialTheme {
        GamesScreen(
            uiState = GamesUiState,
            onEvent = {},
            navTo = {}
        )
    }
}
