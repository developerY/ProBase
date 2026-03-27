package com.zoewave.probase.goswift.mobile.shots.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoewave.probase.goswift.features.main.navigation.GoSwiftDestination
import com.zoewave.probase.goswift.mobile.shots.ui.components.ShotItem

@Composable
fun ShotsUiRoute(
    modifier: Modifier = Modifier,
    viewModel: ShotsViewModel = hiltViewModel(),
    navTo: (GoSwiftDestination) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ShotsScreen(
        modifier = modifier,
        uiState = uiState,
        onEvent = viewModel::onEvent,
        navTo = navTo
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShotsScreen(
    modifier: Modifier = Modifier,
    uiState: ShotsUiState,
    onEvent: (ShotsUiEvent) -> Unit,
    navTo: (GoSwiftDestination) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Caffeine Shots") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navTo(GoSwiftDestination.AddShot) }) {
                Icon(Icons.Default.Add, contentDescription = "Add Shot")
            }
        },
        modifier = modifier
    ) { padding ->
        when (uiState) {
            ShotsUiState.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is ShotsUiState.Success -> {
                if (uiState.shots.isEmpty()) {
                    Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                        Text("No shots recorded today.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.shots, key = { it.id }) { shot ->
                            ShotItem(
                                shot = shot,
                                onDelete = { onEvent(ShotsUiEvent.DeleteShot(shot.id)) }
                            )
                        }
                    }
                }
            }
        }
    }
}
