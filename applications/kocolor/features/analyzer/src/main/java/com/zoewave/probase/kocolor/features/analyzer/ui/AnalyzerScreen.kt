package com.zoewave.probase.kocolor.features.analyzer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.kocolor.features.analyzer.R
import com.zoewave.probase.kocolor.model.KoColorRoute

@Preview(showBackground = true)
@Composable
private fun AnalyzerUiRoutePreview() {
    MaterialTheme {
        AnalyzerUiRoute(
            uiState = AnalyzerScreenUiState(),
            onEvent = {},
            navTo = {}
        )
    }
}

@Composable
fun AnalyzerUiRoute(
    uiState: AnalyzerScreenUiState,
    onEvent: (AnalyzerEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    AnalyzerScreen(
        uiState = uiState,
        onEvent = onEvent,
        navTo = navTo
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyzerScreen(
    uiState: AnalyzerScreenUiState,
    onEvent: (AnalyzerEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.applications_kocolor_features_analyzer_title)) },
                navigationIcon = {
                    IconButton(onClick = { navTo(KoColorRoute.Back) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.applications_kocolor_features_analyzer_back))
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState.analyzerState) {
                is AnalyzerUiState.Idle -> {
                    StyleCaptureState(
                        uiState = uiState,
                        onEvent = onEvent,
                        navTo = navTo
                    )
                }
                is AnalyzerUiState.Loading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(stringResource(R.string.applications_kocolor_features_analyzer_loading))
                    }
                }
                is AnalyzerUiState.Success -> {
                    AnalysisResultScreen(
                        uiState = state.advice,
                        onEvent = { event ->
                            if (event is AnalyzerEvent.OnSaveClicked) {
                                onEvent(event)
                                navTo(KoColorRoute.Back)
                            } else {
                                onEvent(event)
                            }
                        },
                        navTo = navTo
                    )
                }
                is AnalyzerUiState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                        Button(onClick = { onEvent(AnalyzerEvent.OnResetClicked) }, modifier = Modifier.padding(top = 16.dp)) {
                            Text(stringResource(R.string.applications_kocolor_features_analyzer_try_again))
                        }
                    }
                }
            }
        }
    }
}
