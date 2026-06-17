package com.zoewave.probase.kocolor.features.routines.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.kocolor.model.KoColorRoute

@Composable
fun DailyInsightSmall(
    uiState: Unit,
    modifier: Modifier = Modifier,
    onEvent: (Unit) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    Surface(
        color = Color(0xFFFDF0ED),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = "Daily Insight", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = Color(0xFF8B5E3C))
            Text(
                text = "\"Patience is the foundation of every lasting ritual.\"",
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Serif,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DailyInsightSmallPreview() {
    MaterialTheme {
        DailyInsightSmall(uiState = Unit, onEvent = {}, navTo = {})
    }
}
