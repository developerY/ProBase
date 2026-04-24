package com.zoewave.probase.kocolor.mobile.features.settings.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoewave.probase.features.ai.configuration.ui.AiConfigurationCard
import com.zoewave.probase.kocolor.mobile.features.settings.ui.SettingsEvent
import com.zoewave.probase.kocolor.mobile.features.settings.ui.SettingsUiState
import com.zoewave.probase.kocolor.mobile.features.settings.ui.SettingsViewModel
import com.zoewave.probase.kocolor.model.KoColorRoute

@Composable
fun SettingsUiRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SettingsScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        navTo = { route -> if (route == null) onBack() },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onEvent: (SettingsEvent) -> Unit,
    navTo: (KoColorRoute?) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navTo(null) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ThemeSettingsCard(
                expanded = uiState.isThemeExpanded,
                onExpandToggle = { onEvent(SettingsEvent.OnThemeExpandedToggled(!uiState.isThemeExpanded)) },
                currentTheme = uiState.currentTheme,
                onThemeSelected = { onEvent(SettingsEvent.OnThemeSelected(it)) }
            )

            PaletteSettingsCard(
                expanded = uiState.isPaletteExpanded,
                onExpandToggle = { onEvent(SettingsEvent.OnPaletteExpandedToggled(!uiState.isPaletteExpanded)) },
                currentPalette = uiState.currentPalette,
                onPaletteSelected = { onEvent(SettingsEvent.OnPaletteSelected(it)) }
            )

            AiConfigurationCard(
                expanded = uiState.isAiExpanded,
                onExpandToggle = { onEvent(SettingsEvent.OnAiExpandedToggled(!uiState.isAiExpanded)) },
                title = "AI Configuration",
                description = "Configure your Gemini API Key for style analysis and personal suggestions."
            )
            
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("About", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("KoColor Fashion App v0.1.0")
                    Text("Powered by Gemini AI")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    MaterialTheme {
        SettingsScreen(
            uiState = SettingsUiState(
                currentTheme = "DARK",
                currentPalette = "PASTEL"
            ),
            onEvent = {},
            navTo = {}
        )
    }
}
