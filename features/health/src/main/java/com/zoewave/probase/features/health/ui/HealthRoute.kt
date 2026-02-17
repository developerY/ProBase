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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
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
                HealthDataScreen(weeklySteps = state.weeklySteps,
                    weeklyDistance = state.weeklyDistance,
                    weeklyCalories = state.weeklyCalories)
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
private fun HealthDataScreen(
    weeklySteps: Map<String, Long>,
    weeklyDistance: Map<String, Double>,
    weeklyCalories: Map<String, Double>
) {
    val settingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()), // Make screen scrollable
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Success Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(top = 16.dp, bottom = 24.dp)
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

        Text(
            text = "Last 7 Days Activity",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // 1. Steps Chart
        GenericWeeklyChart(
            title = "Steps",
            data = weeklySteps.mapValues { it.value.toDouble() },
            color = MaterialTheme.colorScheme.primary,
            formatValue = { v -> if (v > 999) "${(v / 1000).toInt()}k" else "${v.toInt()}" }
        )

        // 2. Calories Chart
        GenericWeeklyChart(
            title = "Calories (kcal)",
            data = weeklyCalories,
            color = Color(0xFFFF9800), // Orange
            formatValue = { v -> "${v.toInt()}" }
        )

        // 3. Distance Chart
        GenericWeeklyChart(
            title = "Distance (km)",
            data = weeklyDistance,
            color = Color(0xFF03A9F4), // Blue
            formatValue = { v -> String.format("%.1f", v / 1000) } // Convert meters to km
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = {
                val intent = Intent(HealthConnectClient.ACTION_HEALTH_CONNECT_SETTINGS)
                settingsLauncher.launch(intent)
            },
            modifier = Modifier.padding(bottom = 32.dp)
        ) {
            Text("Manage Permissions")
        }
    }
}

/**
 * Reusable Chart Component
 */
@Composable
fun GenericWeeklyChart(
    title: String,
    data: Map<String, Double>,
    color: Color,
    formatValue: (Double) -> String
) {
    val maxVal = data.values.maxOrNull() ?: 1.0
    val sortedData = data.toSortedMap()

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))

            if (data.values.sum() == 0.0) {
                Text("No data", style = MaterialTheme.typography.bodySmall)
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    sortedData.forEach { (dateStr, value) ->
                        val dayLabel = try {
                            LocalDate.parse(dateStr).dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                        } catch (e: Exception) {
                            dateStr.takeLast(2)
                        }

                        val barHeightFraction = (value.toFloat() / maxVal.toFloat()).coerceAtLeast(0.02f)

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = formatValue(value),
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                fontSize = 10.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .width(12.dp)
                                    .fillMaxHeight(barHeightFraction)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(color)
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
    }
}