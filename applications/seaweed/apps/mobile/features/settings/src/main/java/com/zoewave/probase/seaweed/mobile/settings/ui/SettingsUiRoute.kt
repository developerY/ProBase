package com.zoewave.probase.seaweed.mobile.settings.ui

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
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionHeader(stringResource(R.string.applications_seaweed_apps_mobile_features_settings_income))
            
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                OutlinedTextField(
                    value = settings.monthlyIncome.toString(),
                    onValueChange = { 
                        val income = it.toDoubleOrNull() ?: 0.0
                        onEvent(SettingsUiEvent.UpdateIncome(income)) 
                    },
                    label = { Text(stringResource(R.string.applications_seaweed_apps_mobile_features_settings_monthly_income)) },
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            SectionHeader(stringResource(R.string.applications_seaweed_apps_mobile_features_settings_theme_appearance))
            ThemeConfigSelectionGroup(
                currentConfig = settings.themeConfig,
                onConfigSelected = { onEvent(SettingsUiEvent.UpdateTheme(it)) }
            )

            SectionHeader(stringResource(R.string.applications_seaweed_apps_mobile_features_settings_theme_mode))
            ThemeModeSelectionGroup(
                currentMode = settings.themeMode,
                onModeSelected = { onEvent(SettingsUiEvent.UpdateThemeMode(it)) }
            )

            SectionHeader(stringResource(R.string.applications_seaweed_apps_mobile_features_settings_receipt_ai_title))
            var isAiExpanded by remember { mutableStateOf(false) }
            AiConfigurationCard(
                expanded = isAiExpanded,
                onExpandToggle = { isAiExpanded = !isAiExpanded },
                title = stringResource(R.string.applications_seaweed_apps_mobile_features_settings_receipt_ai_title),
                description = stringResource(R.string.applications_seaweed_apps_mobile_features_settings_receipt_ai_desc)
            )

            SectionHeader(stringResource(R.string.applications_seaweed_apps_mobile_features_settings_privacy_policy))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column {
                    SettingsLinkItem(
                        label = stringResource(R.string.applications_seaweed_apps_mobile_features_settings_privacy_policy),
                        icon = Icons.Default.PrivacyTip,
                        onClick = { onEvent(SettingsUiEvent.NavigateTo(SeaweedDestination.PrivacyPolicy)) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    SettingsLinkItem(
                        label = stringResource(R.string.applications_seaweed_apps_mobile_features_settings_data_deletion),
                        icon = Icons.Default.Delete,
                        onClick = { onEvent(SettingsUiEvent.NavigateTo(SeaweedDestination.DataDeletion)) }
                    )
                }
            }

            SectionHeader(stringResource(R.string.applications_seaweed_apps_mobile_features_settings_about))
            AboutSection(deviceId = uiState.firebaseDeviceId)

            SectionHeader(stringResource(R.string.applications_seaweed_apps_mobile_features_settings_developer_options), color = MaterialTheme.colorScheme.error)
            Button(
                onClick = { onEvent(SettingsUiEvent.GenerateTestData) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer)
            ) {
                Text(stringResource(R.string.applications_seaweed_apps_mobile_features_settings_generate_test_data))
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary
) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
        color = color,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun AboutSection(deviceId: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.applications_seaweed_apps_mobile_features_settings_app_instance_id),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Text(
                    text = deviceId,
                    modifier = Modifier.padding(12.dp),
                    style = androidx.compose.ui.text.TextStyle(
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        fontSize = 11.sp
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
}

@Composable
private fun ThemeConfigSelectionGroup(
    currentConfig: SeaweedThemeConfig,
    onConfigSelected: (SeaweedThemeConfig) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TonalSelectableCard(
            label = stringResource(R.string.applications_seaweed_apps_mobile_features_settings_theme_default),
            isSelected = currentConfig == SeaweedThemeConfig.DEFAULT,
            onClick = { onConfigSelected(SeaweedThemeConfig.DEFAULT) },
            modifier = Modifier.weight(1f)
        )
        TonalSelectableCard(
            label = stringResource(R.string.applications_seaweed_apps_mobile_features_settings_theme_coral),
            isSelected = currentConfig == SeaweedThemeConfig.CORAL,
            onClick = { onConfigSelected(SeaweedThemeConfig.CORAL) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ThemeModeSelectionGroup(
    currentMode: ThemeMode,
    onModeSelected: (ThemeMode) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TonalSelectableCard(
            label = stringResource(R.string.applications_seaweed_apps_mobile_features_settings_mode_system),
            isSelected = currentMode == ThemeMode.SYSTEM,
            onClick = { onModeSelected(ThemeMode.SYSTEM) },
            modifier = Modifier.weight(1f)
        )
        TonalSelectableCard(
            label = stringResource(R.string.applications_seaweed_apps_mobile_features_settings_mode_light),
            isSelected = currentMode == ThemeMode.LIGHT,
            onClick = { onModeSelected(ThemeMode.LIGHT) },
            modifier = Modifier.weight(1f)
        )
        TonalSelectableCard(
            label = stringResource(R.string.applications_seaweed_apps_mobile_features_settings_mode_dark),
            isSelected = currentMode == ThemeMode.DARK,
            onClick = { onModeSelected(ThemeMode.DARK) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TonalSelectableCard(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
            )
        }
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
            modifier = Modifier.padding(16.dp),
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
