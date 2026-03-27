package com.zoewave.probase.goswift.mobile.shots.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoewave.probase.goswift.features.main.navigation.GoSwiftDestination

@Composable
fun AddShotUiRoute(
    modifier: Modifier = Modifier,
    viewModel: AddShotViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onBack()
        }
    }

    AddShotScreen(
        modifier = modifier,
        uiState = uiState,
        onEvent = { event ->
            if (event is AddShotUiEvent.BackClicked) {
                onBack()
            } else {
                viewModel.onEvent(event)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddShotScreen(
    modifier: Modifier = Modifier,
    uiState: AddShotUiState,
    onEvent: (AddShotUiEvent) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Caffeine Shot") },
                navigationIcon = {
                    IconButton(onClick = { onEvent(AddShotUiEvent.BackClicked) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.mg,
                onValueChange = { onEvent(AddShotUiEvent.MgChanged(it)) },
                label = { Text("Caffeine (mg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = { onEvent(AddShotUiEvent.SaveShot) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Log Shot")
            }
        }
    }
}
