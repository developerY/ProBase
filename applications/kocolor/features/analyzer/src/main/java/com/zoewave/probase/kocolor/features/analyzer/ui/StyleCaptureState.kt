package com.zoewave.probase.kocolor.features.analyzer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.kocolor.features.analyzer.R
import com.zoewave.probase.kocolor.features.analyzer.ui.components.*
import com.zoewave.probase.kocolor.model.KoColorRoute

@Composable
fun StyleCaptureState(
    uiState: AnalyzerScreenUiState,
    onEvent: (AnalyzerEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            stringResource(R.string.applications_kocolor_features_analyzer_capture_instruction),
            style = MaterialTheme.typography.titleMedium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Column(
            modifier = Modifier.height(400.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StyleCaptureSlot(
                    uiState = StyleCaptureSlotUiState(
                        title = stringResource(R.string.applications_kocolor_features_analyzer_face_label),
                        uri = uiState.faceUri
                    ),
                    modifier = Modifier.weight(1f),
                    onEvent = onEvent,
                    navTo = { navTo(KoColorRoute.Camera("face")) }
                )
                StyleCaptureSlot(
                    uiState = StyleCaptureSlotUiState(
                        title = stringResource(R.string.applications_kocolor_features_analyzer_hair_label),
                        uri = uiState.hairUri
                    ),
                    modifier = Modifier.weight(1f),
                    onEvent = onEvent,
                    navTo = { navTo(KoColorRoute.Camera("hair")) }
                )
            }
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StyleCaptureSlot(
                    uiState = StyleCaptureSlotUiState(
                        title = stringResource(R.string.applications_kocolor_features_analyzer_shoes_label),
                        uri = uiState.shoesUri
                    ),
                    modifier = Modifier.weight(1f),
                    onEvent = onEvent,
                    navTo = { navTo(KoColorRoute.Camera("shoes")) }
                )
                StyleCaptureSlot(
                    uiState = StyleCaptureSlotUiState(
                        title = stringResource(R.string.applications_kocolor_features_analyzer_clothes_label),
                        uri = uiState.clothesUri
                    ),
                    modifier = Modifier.weight(1f),
                    onEvent = onEvent,
                    navTo = { navTo(KoColorRoute.Camera("clothes")) }
                )
            }
        }

        OccasionFilter(
            uiState = OccasionFilterUiState(uiState.selectedOccasion),
            onEvent = onEvent,
            navTo = {}
        )

        LocationInput(
            uiState = LocationInputUiState(uiState.locationName, uiState.isLocating),
            onEvent = onEvent,
            navTo = {}
        )

        Button(
            onClick = { onEvent(AnalyzerEvent.OnAnalyzeClicked) },
            enabled = uiState.faceUri != null || uiState.hairUri != null || uiState.shoesUri != null || uiState.clothesUri != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.applications_kocolor_features_analyzer_analyze_action))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StyleCaptureStatePreview() {
    MaterialTheme {
        StyleCaptureState(
            uiState = AnalyzerScreenUiState(),
            onEvent = {},
            navTo = {}
        )
    }
}
