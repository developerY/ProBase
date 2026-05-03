package com.zoewave.probase.seaweed.mobile.settings.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoewave.probase.features.ai.configuration.ui.AiConfigurationCard
import com.zoewave.probase.seaweed.mobile.settings.R
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

    SettingsScreen(
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
    navTo: (SeaweedDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.applications_seaweed_apps_mobile_features_settings_title)) })
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
                SettingsContent(
                    uiState = uiState,
                    onEvent = onEvent,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
private fun SettingsContent(
    uiState: SettingsUiState.Success,
    onEvent: (SettingsUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val settings = uiState.settings
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(stringResource(R.string.applications_seaweed_apps_mobile_features_settings_income), style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(
            value = settings.monthlyIncome.toString(),
            onValueChange = { val income = it.toDoubleOrNull() ?: 0.0; onEvent(SettingsUiEvent.UpdateIncome(income)) },
            label = { Text(stringResource(R.string.applications_seaweed_apps_mobile_features_settings_monthly_income)) },
            modifier = Modifier.fillMaxWidth()
        )

        Text(stringResource(R.string.applications_seaweed_apps_mobile_features_settings_theme_appearance), style = MaterialTheme.typography.titleLarge)
        ThemeConfigSelectionGroup(
            currentConfig = settings.themeConfig,
            onConfigSelected = { onEvent(SettingsUiEvent.UpdateTheme(it)) }
        )

        Text(stringResource(R.string.applications_seaweed_apps_mobile_features_settings_theme_mode), style = MaterialTheme.typography.titleLarge)
        ThemeModeSelectionGroup(
            currentMode = settings.themeMode,
            onModeSelected = { onEvent(SettingsUiEvent.UpdateThemeMode(it)) }
        )

        var isAiExpanded by remember { mutableStateOf(false) }
        AiConfigurationCard(
            expanded = isAiExpanded,
            onExpandToggle = { isAiExpanded = !isAiExpanded },
            title = stringResource(R.string.applications_seaweed_apps_mobile_features_settings_receipt_ai_title),
            description = stringResource(R.string.applications_seaweed_apps_mobile_features_settings_receipt_ai_desc)
        )

        HorizontalDivider()

        SettingsLinkItem(
            label = stringResource(R.string.applications_seaweed_apps_mobile_features_settings_privacy_policy),
            icon = Icons.Default.PrivacyTip,
            onClick = { onEvent(SettingsUiEvent.NavigateTo(SeaweedDestination.PrivacyPolicy)) }
        )

        SettingsLinkItem(
            label = stringResource(R.string.applications_seaweed_apps_mobile_features_settings_data_deletion),
            icon = Icons.Default.Delete,
            onClick = { onEvent(SettingsUiEvent.NavigateTo(SeaweedDestination.DataDeletion)) }
        )

        HorizontalDivider()

        AboutSection(deviceId = uiState.firebaseDeviceId)

        HorizontalDivider()

        Text(stringResource(R.string.applications_seaweed_apps_mobile_features_settings_developer_options), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.error)
        Button(
            onClick = { onEvent(SettingsUiEvent.GenerateTestData) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer)
        ) {
            Text(stringResource(R.string.applications_seaweed_apps_mobile_features_settings_generate_test_data))
        }
    }
}

@Composable
private fun AboutSection(deviceId: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.applications_seaweed_apps_mobile_features_settings_about), style = MaterialTheme.typography.titleLarge)
        Text(
            text = stringResource(R.string.applications_seaweed_apps_mobile_features_settings_app_instance_id),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = deviceId,
                modifier = Modifier.padding(12.dp),
                style = androidx.compose.ui.text.TextStyle(
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    fontSize = 12.sp
                )
            )
        }
        Text(
            text = stringResource(R.string.applications_seaweed_apps_mobile_features_settings_deletion_request_instruction),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ThemeConfigSelectionGroup(
    currentConfig: SeaweedThemeConfig,
    onConfigSelected: (SeaweedThemeConfig) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ThemeOption(
            label = stringResource(R.string.applications_seaweed_apps_mobile_features_settings_theme_default),
            isSelected = currentConfig == SeaweedThemeConfig.DEFAULT,
            onClick = { onConfigSelected(SeaweedThemeConfig.DEFAULT) }
        )
        ThemeOption(
            label = stringResource(R.string.applications_seaweed_apps_mobile_features_settings_theme_coral),
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
            label = stringResource(R.string.applications_seaweed_apps_mobile_features_settings_mode_system),
            isSelected = currentMode == ThemeMode.SYSTEM,
            onClick = { onModeSelected(ThemeMode.SYSTEM) }
        )
        ThemeOption(
            label = stringResource(R.string.applications_seaweed_apps_mobile_features_settings_mode_light),
            isSelected = currentMode == ThemeMode.LIGHT,
            onClick = { onModeSelected(ThemeMode.LIGHT) }
        )
        ThemeOption(
            label = stringResource(R.string.applications_seaweed_apps_mobile_features_settings_mode_dark),
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
            .height(56.dp)
            .clickable(onClick = onClick),
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

@Composable
private fun SettingsLinkItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = androidx.compose.ui.graphics.Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenSuccessPreview() {
    MaterialTheme {
        SettingsScreen(
            uiState = SettingsUiState.Success(
                settings = UserSettings(
                    monthlyIncome = 5000.0,
                    themeConfig = SeaweedThemeConfig.DEFAULT,
                    themeMode = ThemeMode.SYSTEM
                ),
                firebaseDeviceId = "test-id-123"
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
