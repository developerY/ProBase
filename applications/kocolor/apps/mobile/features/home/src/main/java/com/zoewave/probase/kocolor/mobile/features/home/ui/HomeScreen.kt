package com.zoewave.probase.kocolor.mobile.features.home.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoewave.probase.kocolor.model.FashionProfile
import com.zoewave.probase.kocolor.model.SeasonalType
import com.zoewave.probase.kocolor.model.Undertone
import com.zoewave.probase.kocolor.model.KoColorRoute

@Composable
fun HomeUiRoute(
    onNavigateTo: (KoColorRoute) -> Unit,
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        navTo = onNavigateTo,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onEvent: (HomeEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("KoColor Fashion") },
                actions = {
                    IconButton(onClick = { navTo(KoColorRoute.Settings) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProfileSummaryCard(uiState.fashionProfile)

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { navTo(KoColorRoute.Analyzer()) },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Analyze My Style")
            }

            if (uiState.fashionProfile != null) {
                Button(
                    onClick = { navTo(KoColorRoute.Suggestions) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Get Daily Suggestions")
                }
            }
        }
    }
}

@Composable
fun ProfileSummaryCard(profile: FashionProfile?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            if (profile == null) {
                Text("Welcome to KoColor!", style = MaterialTheme.typography.headlineSmall)
                Text("Start by analyzing your seasonal color type for personalized fashion advice.", modifier = Modifier.padding(top = 8.dp))
            } else {
                Text("Your Style Profile", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Seasonal Type: ${profile.seasonalType}", style = MaterialTheme.typography.headlineMedium)
                Text("Undertone: ${profile.undertone}", style = MaterialTheme.typography.titleLarge)
                if (!profile.notes.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(profile.notes!!, maxLines = 3, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview_NoProfile() {
    MaterialTheme {
        HomeScreen(
            uiState = HomeUiState(),
            onEvent = {},
            navTo = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview_WithProfile() {
    MaterialTheme {
        HomeScreen(
            uiState = HomeUiState(
                fashionProfile = FashionProfile(
                    seasonalType = SeasonalType.WINTER,
                    undertone = Undertone.COOL,
                    notes = "Your best colors are deep blues and jewel tones."
                )
            ),
            onEvent = {},
            navTo = {}
        )
    }
}
