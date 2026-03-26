package com.zoewave.probase.mobile.features.settings.ui.components

import com.zoewave.probase.mobile.features.settings.SettingsUiState
import com.zoewave.probase.mobile.features.settings.ui.SettingsEvent

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
import com.zoewave.photodo.model.navigation.PhotoTodoRoute
import com.zoewave.probase.mobile.features.settings.ui.components.ThemeIdentifiers
import com.zoewave.probase.mobile.features.settings.ui.components.ThemeSettingsCard

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
        }
    }
}