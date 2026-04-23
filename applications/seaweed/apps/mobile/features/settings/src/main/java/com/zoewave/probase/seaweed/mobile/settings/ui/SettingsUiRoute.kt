package com.zoewave.probase.seaweed.mobile.settings.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoewave.probase.features.ai.configuration.ui.AiConfigurationCard
import com.zoewave.probase.seaweed.model.SeaweedThemeConfig
import com.zoewave.probase.seaweed.model.ThemeMode
import com.zoewave.probase.seaweed.model.UserSettings
import com.zoewave.probase.seaweed.model.navigation.SeaweedDestination

@Composable
fun SettingsUiRoute(
    navTo: (SeaweedDestination) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SettingsUiRoute(
        uiState = uiState,
        onEvent = { event ->
            if (event is SettingsUiEvent.NavigateTo) {
                navTo(event.destination)
            } else {
                viewModel.onEvent(event)
            }
        },
        navTo = navTo,
        modifier = modifier
    )
}

@Composable
internal fun SettingsUiRoute(
    uiState: SettingsUiState,
    onEvent: (SettingsUiEvent) -> Unit,
    navTo: (SeaweedDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    SettingsScreen(
        uiState = uiState,
        onEvent = onEvent,
        navTo = navTo,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onEvent: (SettingsUiEvent) -> Unit,
    @Suppress("UnusedParameter") navTo: (SeaweedDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Settings") })
        },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        when (uiState) {
            SettingsUiState.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is SettingsUiState.Success -> {
                val settings = uiState.settings
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Text("Income", style = MaterialTheme.typography.titleLarge)
                    OutlinedTextField(
                        value = settings.monthlyIncome.toString(),
                        onValueChange = { val income = it.toDoubleOrNull() ?: 0.0; onEvent(SettingsUiEvent.UpdateIncome(income)) },
                        label = { Text("Monthly Income") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Theme Appearance", style = MaterialTheme.typography.titleLarge)
                    ThemeConfigSelectionGroup(
                        currentConfig = settings.themeConfig,
                        onConfigSelected = { onEvent(SettingsUiEvent.UpdateTheme(it)) }
                    )

                    Text("Theme Mode", style = MaterialTheme.typography.titleLarge)
                    ThemeModeSelectionGroup(
                        currentMode = settings.themeMode,
                        onModeSelected = { onEvent(SettingsUiEvent.UpdateThemeMode(it)) }
                    )

                    var isAiExpanded by remember { mutableStateOf(false) }
                    AiConfigurationCard(
                        expanded = isAiExpanded,
                        onExpandToggle = { isAiExpanded = !isAiExpanded },
                        title = "Seaweed Receipt AI",
                        description = "Use Gemini to automatically extract merchant and amount from receipts."
                    )

                    HorizontalDivider()

                    Text("Developer Options", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.error)
                    Button(
                        onClick = { onEvent(SettingsUiEvent.GenerateTestData) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer)
                    ) {
                        Text("Generate 3 Months of Random Data")
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeConfigSelectionGroup(
    currentConfig: SeaweedThemeConfig,
    onConfigSelected: (SeaweedThemeConfig) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ThemeOption(
            label = "Default",
            isSelected = currentConfig == SeaweedThemeConfig.DEFAULT,
            onClick = { onConfigSelected(SeaweedThemeConfig.DEFAULT) }
        )
        ThemeOption(
            label = "Coral",
            isSelected = currentConfig == SeaweedThemeConfig.CORAL,
            onClick = { onConfigSelected(SeaweedThemeConfig.CORAL) }
        )
    }
}

@Composable
private fun ThemeModeSelectionGroup(
    currentMode: ThemeMode,
    onModeSelected: (ThemeMode) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ThemeOption(
            label = "System Default",
            isSelected = currentMode == ThemeMode.SYSTEM,
            onClick = { onModeSelected(ThemeMode.SYSTEM) }
        )
        ThemeOption(
            label = "Light",
            isSelected = currentMode == ThemeMode.LIGHT,
            onClick = { onModeSelected(ThemeMode.LIGHT) }
        )
        ThemeOption(
            label = "Dark",
            isSelected = currentMode == ThemeMode.DARK,
            onClick = { onModeSelected(ThemeMode.DARK) }
        )
    }
}

@Composable
private fun ThemeOption(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick
        )
        Text(
            text = label,
            modifier = Modifier
                .padding(start = 16.dp)
                .weight(1f),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ThemeOptionPreview() {
    MaterialTheme {
        ThemeOption(label = "Light", isSelected = true, onClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsUiRoutePreview() {
    MaterialTheme {
        SettingsUiRoute(
            uiState = SettingsUiState.Success(
                settings = UserSettings(
                    monthlyIncome = 5000.0,
                    themeConfig = SeaweedThemeConfig.DEFAULT,
                    themeMode = ThemeMode.SYSTEM
                )
            ),
            onEvent = {},
            navTo = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    MaterialTheme {
        SettingsScreen(
            uiState = SettingsUiState.Success(
                settings = UserSettings(
                    monthlyIncome = 5000.0,
                    themeConfig = SeaweedThemeConfig.DEFAULT,
                    themeMode = ThemeMode.SYSTEM
                )
            ),
            onEvent = {},
            navTo = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenLoadingPreview() {
    MaterialTheme {
        SettingsScreen(
            uiState = SettingsUiState.Loading,
            onEvent = {},
            navTo = {}
        )
    }
}
