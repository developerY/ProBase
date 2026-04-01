package com.zoewave.probase.photodo.mobile.features.settings.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.photodo.mobile.core.ui.theme.PhotoDoTheme
import com.zoewave.probase.photodo.mobile.features.settings.R
import com.zoewave.probase.photodo.mobile.features.settings.ui.SettingsEvent
import com.zoewave.probase.photodo.mobile.features.settings.ui.SettingsUiState
import com.zoewave.probase.photodo.model.navigation.PhotoTodoRoute


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onEvent: (SettingsEvent) -> Unit,
    navTo: (PhotoTodoRoute?) -> Unit,
    modifier: Modifier = Modifier
) {
    // Determine if we should open this card automatically based on the UiState deep-link
    var isThemeExpanded by rememberSaveable(uiState.initialCardKeyToExpand) {
        mutableStateOf(uiState.initialCardKeyToExpand == ThemeIdentifiers.SYSTEM)
    }

    var isAboutExpanded by rememberSaveable(uiState.initialCardKeyToExpand) {
        mutableStateOf(uiState.initialCardKeyToExpand == "ABOUT")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.applications_photodo_apps_mobile_features_settings_title)) },
                navigationIcon = {
                    // navTo(null) acts as our standard "Pop Backstack" action
                    IconButton(onClick = { navTo(null) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.applications_photodo_apps_mobile_features_settings_back_content_description)
                        )
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 600.dp) // 🚀 Constraint width for foldable/tablet!
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                ThemeSettingsCard(
                    title = stringResource(R.string.applications_photodo_apps_mobile_features_settings_app_theme_title),
                    expanded = isThemeExpanded,
                    onExpandToggle = { isThemeExpanded = !isThemeExpanded },
                    currentTheme = uiState.currentTheme,
                    onThemeSelected = { newTheme ->
                        onEvent(SettingsEvent.OnThemeSelected(newTheme))
                    }
                )

                PaletteSettingsCard(
                    currentPalette = uiState.currentPalette,
                    onPaletteSelected = { newPalette ->
                        onEvent(SettingsEvent.OnPaletteSelected(newPalette))
                    }
                )

                // --- TWO-PANE CONTRAST TOGGLE ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Two-Pane Contrast",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Apply a subtle tint to the dashboard side on large screens",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = uiState.currentPaneContrast == "TINTED",
                        onCheckedChange = { isChecked ->
                            val newOption = if (isChecked) "TINTED" else "FLAT"
                            onEvent(SettingsEvent.OnPaneContrastSelected(newOption))
                        }
                    )
                }

                AboutSettingsCard(
                    expanded = isAboutExpanded,
                    onExpandToggle = { isAboutExpanded = !isAboutExpanded },
                    appVersion = uiState.appVersion,
                    firebaseDeviceId = uiState.firebaseDeviceId
                )

                // Additional settings cards can be added here following the same pattern
                /*
                NotificationSettingsCard(
                    uiState = uiState,
                    onEvent = onEvent
                )
                */
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    PhotoDoTheme {
        SettingsScreen(
            uiState = SettingsUiState(
                currentTheme = ThemeIdentifiers.SYSTEM,
                initialCardKeyToExpand = ThemeIdentifiers.SYSTEM
            ),
            onEvent = {},
            navTo = {}
        )
    }
}
