package com.zoewave.probase.kocolor.features.analyzer.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.kocolor.features.analyzer.R
import com.zoewave.probase.kocolor.features.analyzer.ui.AnalyzerEvent
import com.zoewave.probase.kocolor.model.KoColorRoute

@Composable
fun OccasionFilter(
    uiState: String,
    onEvent: (AnalyzerEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val occasions = listOf(
        stringResource(R.string.applications_kocolor_features_analyzer_occasion_work),
        stringResource(R.string.applications_kocolor_features_analyzer_occasion_date_night),
        stringResource(R.string.applications_kocolor_features_analyzer_occasion_outdoor_sport),
        stringResource(R.string.applications_kocolor_features_analyzer_occasion_formal)
    )
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.applications_kocolor_features_analyzer_select_occasion), style = MaterialTheme.typography.labelMedium)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            occasions.forEach { occasion ->
                FilterChip(
                    selected = uiState == occasion,
                    onClick = { onEvent(AnalyzerEvent.OnOccasionSelected(occasion)) },
                    label = { Text(occasion) }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OccasionFilterPreview() {
    MaterialTheme {
        OccasionFilter(
            uiState = "Work",
            onEvent = {},
            navTo = {}
        )
    }
}
