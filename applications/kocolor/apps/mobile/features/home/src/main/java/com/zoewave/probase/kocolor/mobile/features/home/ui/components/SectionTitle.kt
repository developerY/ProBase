package com.zoewave.probase.kocolor.mobile.features.home.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.zoewave.probase.kocolor.model.KoColorRoute

data class SectionTitleUiState(val title: String, val subtitle: String)

@Composable
fun SectionTitle(
    uiState: SectionTitleUiState, 
    modifier: Modifier = Modifier,
    onEvent: (Unit) -> Unit = {}, 
    navTo: (KoColorRoute) -> Unit = {}
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
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
