package com.zoewave.probase.kocolor.mobile.features.settings.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoewave.probase.features.ai.configuration.ui.AiConfigurationCard
import com.zoewave.probase.kocolor.mobile.core.R
import com.zoewave.probase.kocolor.mobile.core.ui.health.HealthContent
import com.zoewave.probase.kocolor.mobile.core.ui.health.HealthUiRoute
import com.zoewave.probase.kocolor.mobile.features.settings.ui.SettingsEvent
import com.zoewave.probase.kocolor.mobile.features.settings.ui.SettingsUiState
import com.zoewave.probase.kocolor.mobile.features.settings.ui.SettingsViewModel
import com.zoewave.probase.kocolor.model.KoColorRoute

@Preview(showBackground = true)
@Composable
private fun SettingsUiRoutePreview() {
    MaterialTheme {
        SettingsUiRoute(
            uiState = SettingsUiState(),
            onEvent = {},
            navTo = {}
        )
    }
}

@Composable
fun SettingsUiRoute(
    uiState: SettingsUiState,
    onEvent: (SettingsEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    SettingsScreen(
        uiState = uiState,
        onEvent = onEvent,
        navTo = navTo
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onEvent: (SettingsEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val uriHandler = LocalUriHandler.current
    val privacyPolicyUrl = stringResource(R.string.applications_kocolor_apps_mobile_core_privacy_policy_url)
    val dataDeletionUrl = stringResource(R.string.applications_kocolor_apps_mobile_core_data_deletion_url)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navTo(KoColorRoute.Back) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
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
                uiState = uiState.isThemeExpanded to uiState.currentTheme,
                onEvent = onEvent,
                navTo = navTo
            )

            PaletteSettingsCard(
                uiState = uiState.isPaletteExpanded to uiState.currentPalette,
                onEvent = onEvent,
                navTo = navTo
            )

            AiConfigurationCard(
                expanded = uiState.isAiExpanded,
                onExpandToggle = { onEvent(SettingsEvent.OnAiExpandedToggled(!uiState.isAiExpanded)) },
                title = "AI Configuration",
                description = "Configure your Gemini API Key for style analysis and personal suggestions."
            )

            HealthConnectCard(
                uiState = uiState.isHealthExpanded,
                onEvent = onEvent,
                navTo = navTo
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    android.util.Log.d("SettingsScreen", "Google XR Test clicked")
                    navTo(KoColorRoute.GoogleXRTest)
                }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Google XR Test", style = MaterialTheme.typography.titleMedium)
                        Text("Launch standard Google " + "First Activity" + " example", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Developer Options", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { onEvent(SettingsEvent.OnGenerateSampleCosmetics) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Add 50 Sample Cosmetics")
                    }
                }
            }
            
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("About", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("KoColor Fashion App v0.1.0")
                    Text("Powered by Gemini AI")
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    TextButton(
                        onClick = { uriHandler.openUri(privacyPolicyUrl) },
                        modifier = Modifier.align(Alignment.Start),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.applications_kocolor_apps_mobile_core_settings_about_privacy_policy),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    TextButton(
                        onClick = { uriHandler.openUri(dataDeletionUrl) },
                        modifier = Modifier.align(Alignment.Start),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.applications_kocolor_apps_mobile_core_settings_about_data_deletion),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HealthConnectCardPreview() {
    MaterialTheme {
        HealthConnectCard(uiState = true, onEvent = {}, navTo = {})
    }
}

@Composable
fun HealthConnectCard(
    uiState: Boolean,
    onEvent: (SettingsEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val expanded = uiState
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onEvent(SettingsEvent.OnHealthExpandedToggled(!expanded)) }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Google Health Connect", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Sync sleep and wellness data for skin analysis",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp 
                                 else Icons.Default.KeyboardArrowDown,
                    contentDescription = null
                )
            }
            if (expanded) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                Box(modifier = Modifier.padding(16.dp)) {
                    val healthViewModel: com.zoewave.probase.features.health.core.ui.HealthViewModel = hiltViewModel()
                    val healthState by healthViewModel.uiState.collectAsStateWithLifecycle()
                    HealthContent(
                        uiState = healthState,
                        onEvent = healthViewModel::onEvent,
                        navTo = navTo,
                        sideEffects = healthViewModel.sideEffect,
                        modifier = Modifier.fillMaxWidth(),
                        statusOnly = true
                    )
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
