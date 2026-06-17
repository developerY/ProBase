package com.zoewave.probase.kocolor.features.analyzer.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.kocolor.features.analyzer.R
import com.zoewave.probase.kocolor.features.analyzer.ui.AnalyzerEvent
import com.zoewave.probase.kocolor.model.KoColorRoute

data class LocationInputUiState(
    val locationName: String?,
    val isLocating: Boolean
)

@Composable
fun LocationInput(
    uiState: LocationInputUiState,
    modifier: Modifier = Modifier,
    onEvent: (AnalyzerEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val locationName = uiState.locationName
    val isLocating = uiState.isLocating

    Column(modifier = modifier.fillMaxWidth()) {
        Text(stringResource(R.string.applications_kocolor_features_analyzer_style_location), style = MaterialTheme.typography.labelMedium)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = locationName ?: "",
                onValueChange = { onEvent(AnalyzerEvent.OnLocationChanged(it.takeIf { it.isNotBlank() })) },
                modifier = Modifier.weight(1f),
                placeholder = { Text(stringResource(R.string.applications_kocolor_features_analyzer_location_placeholder)) },
                label = { Text(stringResource(R.string.applications_kocolor_features_analyzer_local_context_label)) },
                singleLine = true,
                trailingIcon = {
                    if (isLocating) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        IconButton(onClick = { onEvent(AnalyzerEvent.OnDetectLocationClicked) }) {
                            Icon(Icons.Default.MyLocation, contentDescription = stringResource(R.string.applications_kocolor_features_analyzer_detect_location))
                        }
                    }
                }
            )
        }
        Text(
            stringResource(R.string.applications_kocolor_features_analyzer_ai_location_desc),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LocationInputPreview() {
    MaterialTheme {
        LocationInput(
            uiState = LocationInputUiState("New York", false),
            onEvent = {},
            navTo = {}
        )
    }
}
