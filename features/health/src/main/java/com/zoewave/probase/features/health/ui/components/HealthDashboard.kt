package com.zoewave.probase.features.health.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.zoewave.probase.features.health.ui.HealthEvent
import com.zoewave.probase.features.health.ui.HealthUiState
import com.zoewave.probase.features.health.ui.overview.OverviewTab
import com.zoewave.probase.features.health.ui.sessions.SessionsTab
import com.zoewave.probase.features.health.ui.settings.SettingsTab

private enum class HealthTab(val label: String, val icon: ImageVector) {
    Settings("Settings", Icons.Default.Settings),
    Data("Overview", Icons.Default.DateRange),
    Sessions("Sessions", Icons.Default.List)
}

@Composable
fun HealthDashboard(
    state: HealthUiState.Success,
    onEvent: (HealthEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf(HealthTab.Data) }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar {
                HealthTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        icon = { Icon(tab.icon, contentDescription = null) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            when (currentTab) {
                HealthTab.Settings -> SettingsTab(onEvent = onEvent)
                HealthTab.Data -> OverviewTab(state = state)
                HealthTab.Sessions -> SessionsTab(sessions = state.sessions, onEvent = onEvent)
            }
        }
    }
}