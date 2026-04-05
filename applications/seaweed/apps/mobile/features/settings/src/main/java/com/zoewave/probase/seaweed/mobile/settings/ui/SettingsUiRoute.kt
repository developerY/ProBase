package com.zoewave.probase.seaweed.mobile.settings.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoewave.probase.seaweed.model.SeaweedThemeConfig
import com.zoewave.probase.seaweed.model.ThemeMode
import com.zoewave.probase.seaweed.model.navigation.SeaweedDestination

@Composable
fun SettingsUiRoute(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
    navTo: (SeaweedDestination) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SettingsScreen(
        modifier = modifier,
        uiState = uiState,
        onEvent = viewModel::onEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    uiState: SettingsUiState,
    onEvent: (SettingsUiEvent) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Settings") })
        },
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            when (uiState) {
                SettingsUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is SettingsUiState.Success -> {
                    Text(
                        text = "App Color Theme",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                    
                    ThemeConfigSelectionGroup(
                        selectedTheme = uiState.settings.themeConfig,
                        onThemeSelected = { onEvent(SettingsUiEvent.UpdateTheme(it)) }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Text(
                        text = "Theme Mode",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(16.dp)
                    )

                    ThemeModeSelectionGroup(
                        selectedMode = uiState.settings.themeMode,
                        onModeSelected = { onEvent(SettingsUiEvent.UpdateThemeMode(it)) }
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    ListItem(
                        headlineContent = { Text("About Seaweed") },
                        supportingContent = { Text("Version 0.0.1") }
                    )
                }
            }
        }
    }
}

@Composable
fun ThemeConfigSelectionGroup(
    selectedTheme: SeaweedThemeConfig,
    onThemeSelected: (SeaweedThemeConfig) -> Unit
) {
    Column(Modifier.selectableGroup()) {
        ThemeOption(
            text = "Seaweed Teal (Default)",
            selected = selectedTheme == SeaweedThemeConfig.DEFAULT,
            onClick = { onThemeSelected(SeaweedThemeConfig.DEFAULT) }
        )
        ThemeOption(
            text = "Seaweed Coral",
            selected = selectedTheme == SeaweedThemeConfig.CORAL,
            onClick = { onThemeSelected(SeaweedThemeConfig.CORAL) }
        )
    }
}

@Composable
fun ThemeModeSelectionGroup(
    selectedMode: ThemeMode,
    onModeSelected: (ThemeMode) -> Unit
) {
    Column(Modifier.selectableGroup()) {
        ThemeOption(
            text = "System Default",
            selected = selectedMode == ThemeMode.SYSTEM,
            onClick = { onModeSelected(ThemeMode.SYSTEM) }
        )
        ThemeOption(
            text = "Light Mode",
            selected = selectedMode == ThemeMode.LIGHT,
            onClick = { onModeSelected(ThemeMode.LIGHT) }
        )
        ThemeOption(
            text = "Dark Mode",
            selected = selectedMode == ThemeMode.DARK,
            onClick = { onModeSelected(ThemeMode.DARK) }
        )
    }
}

@Composable
fun ThemeOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(56.dp)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null // null recommended for accessibility with screen readers
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}
