package com.zoewave.probase.features.health.ui

import android.content.Intent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.zoewave.probase.features.health.ui.components.ErrorScreen
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun HealthRoute(
    modifier: Modifier = Modifier,
    viewModel: HealthViewModel = hiltViewModel()
) {
    val healthUiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    // Launcher for requesting permissions
    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract(),
        onResult = {
            // When returning from permission screen, force reload
            viewModel.onEvent(HealthEvent.LoadHealthData)
        }
    )

    // Initial Load & Lifecycle Observer
    LaunchedEffect(lifecycleOwner) {
        // Reload data whenever the app comes to the foreground (e.g. returning from Settings)
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.onEvent(HealthEvent.LoadHealthData)
        }
    }

    // Side Effect Listener
    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            if (effect is HealthSideEffect.LaunchPermissions) {
                Log.d("HealthRoute", "Launching Permissions Contract")
                permissionsLauncher.launch(effect.permissions)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        when (val state = healthUiState) {
            is HealthUiState.Success -> {
                HealthDataScreen(weeklySteps = state.weeklySteps)
            }

            is HealthUiState.PermissionsRequired -> {
                PermissionsNotGrantedScreen(
                    onEnableClick = { viewModel.onEvent(HealthEvent.RequestPermissions) }
                )
            }

            is HealthUiState.Error -> {
                ErrorScreen(
                    message = state.message,
                    onRetry = { viewModel.onEvent(HealthEvent.Retry) }
                )
            }

            else -> { // Loading
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun PermissionsNotGrantedScreen(onEnableClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Health Connect Permissions Required",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Please grant access to show your weekly activity.",
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onEnableClick) {
            Text("Grant Permissions")
        }
    }
}

@Composable
private fun HealthDataScreen(weeklySteps: Map<String, Long>) {
    val settingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { }
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Success Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(bottom = 24.dp)
                .background(Color(0xFFE8F5E9), RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Access Granted",
                style = MaterialTheme.typography.labelLarge,
                color = Color(0xFF1B5E20)
            )
        }

        // Data Section
        Text(
            text = "Last 7 Days Activity",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 16.dp)
        )

        if (weeklySteps.isNotEmpty() && weeklySteps.values.sum() > 0) {
            WeeklyStepsChart(stepsData = weeklySteps)
        } else {
            // Empty State
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No Step Data Found", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Permissions are working, but no step data exists for this week.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        OutlinedButton(
            onClick = {
                val intent = Intent(HealthConnectClient.ACTION_HEALTH_CONNECT_SETTINGS)
                settingsLauncher.launch(intent)
            },
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text("Manage Permissions")
        }
    }
}

@Composable
fun WeeklyStepsChart(stepsData: Map<String, Long>) {
    val maxSteps = stepsData.values.maxOrNull() ?: 1L
    val sortedData = stepsData.toSortedMap()

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .height(200.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            sortedData.forEach { (dateStr, steps) ->
                val dayLabel = try {
                    LocalDate.parse(dateStr).dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                } catch (e: Exception) {
                    dateStr.takeLast(2)
                }

                val barHeightFraction = (steps.toFloat() / maxSteps).coerceAtLeast(0.02f)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (steps > 999) "${steps / 1000}k" else steps.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(16.dp)
                            .fillMaxHeight(barHeightFraction)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = dayLabel,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}