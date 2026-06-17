package com.zoewave.probase.features.health.hydration.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

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
                            listOf(Color(0xFF81D4FA).copy(alpha = 0.4f), Color(0xFFE1F5FE).copy(alpha = 0.2f))
                        )
                    )
            )

            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Current Progress", 
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = FontFamily.Serif,
                    color = Color.Black.copy(alpha = 0.7f)
                )

                Spacer(Modifier.height(32.dp))

                WaterDropVisual(
                    progress = progress,
                    modifier = Modifier.size(220.dp)
                )

                Spacer(Modifier.height(32.dp))

                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Serif,
                    color = Color.Black.copy(alpha = 0.8f)
                )
            }
        }
    }
}
