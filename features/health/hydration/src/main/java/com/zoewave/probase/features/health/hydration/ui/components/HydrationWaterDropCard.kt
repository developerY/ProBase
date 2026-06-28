package com.zoewave.probase.features.health.hydration.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.features.health.hydration.R

@Preview(showBackground = true)
@Composable
private fun HydrationWaterDropCardPreviewEmpty() {
    MaterialTheme {
        HydrationWaterDropCard(
            uiState = HydrationWaterDropUiState(currentLiters = 0.5, targetLiters = 2.0),
            onEvent = {},
            navTo = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HydrationWaterDropCardPreviewFull() {
    MaterialTheme {
        HydrationWaterDropCard(
            uiState = HydrationWaterDropUiState(currentLiters = 1.5, targetLiters = 2.0),
            onEvent = {},
            navTo = {}
        )
    }
}


data class HydrationWaterDropUiState(
    val currentLiters: Double,
    val targetLiters: Double
)

@Composable
fun HydrationWaterDropCard(
    uiState: HydrationWaterDropUiState,
    modifier: Modifier = Modifier,
    onEvent: (Unit) -> Unit,
    navTo: (Unit) -> Unit
) {
    val progress = (uiState.currentLiters / uiState.targetLiters).toFloat().coerceIn(0f, 1f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(480.dp) 
            .clickable { onEvent(Unit) },
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF039BE5).copy(alpha = 0.5f), Color(0xFFE1F5FE).copy(alpha = 0.3f))
                        )
                    )
            )

            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.features_health_hydration_current_progress), 
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = FontFamily.Serif,
                    color = Color.Black.copy(alpha = 0.6f)
                )

                Spacer(Modifier.height(24.dp))

                WaterDropVisual(
                    progress = progress,
                    modifier = Modifier.size(300.dp)
                )

                Spacer(Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.features_health_hydration_percent_format, (progress * 100).toInt()),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Serif,
                    color = Color.Black.copy(alpha = 0.9f)
                )
            }
        }
    }
}
