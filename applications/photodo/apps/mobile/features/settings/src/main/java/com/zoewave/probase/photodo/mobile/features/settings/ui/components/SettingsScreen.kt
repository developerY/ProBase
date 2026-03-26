package com.zoewave.probase.photodo.mobile.features.settings.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.zoewave.probase.photodo.mobile.core.ui.PhotoDoTheme
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    // navTo(null) acts as our standard "Pop Backstack" action
                    IconButton(onClick = { navTo(null) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            ThemeSettingsCard(
                title = "App Theme",
                expanded = isThemeExpanded,
                onExpandToggle = { isThemeExpanded = !isThemeExpanded },
                currentTheme = uiState.currentTheme,
                onThemeSelected = { newTheme ->
                    onEvent(SettingsEvent.OnThemeSelected(newTheme))
                }
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
