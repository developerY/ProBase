package com.zoewave.probase.features.health.core.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp

data class HealthSectionTitleUiState(val title: String, val subtitle: String)

@Composable
fun HealthSectionTitle(
    uiState: HealthSectionTitleUiState,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = uiState.title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif
        )
        Text(
            text = uiState.subtitle.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 2.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HealthSectionTitlePreview() {
    MaterialTheme {
        HealthSectionTitle(
            uiState = HealthSectionTitleUiState("Bio-Markers", "Style from the inside out")
        )
    }
}
