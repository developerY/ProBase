package com.zoewave.probase.ashbike.mobile

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoewave.ashbike.mobile.home.ui.HomeViewModel
import com.zoewave.ashbike.mobile.settings.ui.SettingsViewModel
import com.zoewave.ashbike.mobile.ui.components.AshBikeMainScreen
import com.zoewave.probase.core.ui.theme.AshBikeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // ✅ Nav3: We hoist the core ViewModels here if they need to survive the whole app lifecycle
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels()

    // This state will be managed within the composable scope using rememberSaveable
    private val permissionAction = mutableStateOf<PermissionAction?>(null)

    sealed class PermissionAction {
        object RequestPermission : PermissionAction()
        object ShowRationale : PermissionAction()
        object Granted : PermissionAction()
        object Denied : PermissionAction()
        object CheckPermission : PermissionAction() // Initial action
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.d("MainActivity", "Permission Granted")
                permissionAction.value = PermissionAction.Granted
            } else {
                Log.d("MainActivity", "Permission Denied")
                permissionAction.value = PermissionAction.Denied
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initial permission check trigger
        if (savedInstanceState == null) {
            permissionAction.value = PermissionAction.CheckPermission
        }

        setContent {
            val theme by settingsViewModel.theme.collectAsStateWithLifecycle()

            // UI State for Permissions
            var showRationaleDialogState by rememberSaveable { mutableStateOf(false) }
            var currentPermissionStatus by rememberSaveable {
                mutableStateOf(
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                )
            }

            // Bridge Activity Logic -> Compose
            val currentPermissionAction = permissionAction.value

            LaunchedEffect(currentPermissionAction) {
                when (currentPermissionAction) {
                    PermissionAction.CheckPermission -> {
                        when {
                            ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED -> {
                                currentPermissionStatus = true
                                permissionAction.value = PermissionAction.Granted
                            }
                            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                                showRationaleDialogState = true
                                permissionAction.value = null
                            }
                            else -> {
                                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                permissionAction.value = null
                            }
                        }
                    }
                    PermissionAction.RequestPermission -> {
                        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        permissionAction.value = null
                    }
                    PermissionAction.Granted -> {
                        currentPermissionStatus = true
                        showRationaleDialogState = false
                        permissionAction.value = null
                    }
                    PermissionAction.Denied -> {
                        currentPermissionStatus = false
                        showRationaleDialogState = false
                        permissionAction.value = null
                    }
                    PermissionAction.ShowRationale -> {
                        showRationaleDialogState = true
                        permissionAction.value = null
                    }
                    null -> { /* Idle */ }
                }
            }

            AshBikeTheme(theme = theme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (showRationaleDialogState) {
                        LocationPermissionRationaleDialog(
                            onConfirm = {
                                showRationaleDialogState = false
                                permissionAction.value = PermissionAction.RequestPermission
                            },
                            onDismiss = {
                                showRationaleDialogState = false
                                permissionAction.value = PermissionAction.Denied
                            }
                        )
                    } else if (currentPermissionStatus) {
                        // ✅ Nav3: REPLACED RootNavGraph with BikeApp
                        // BikeApp contains the NavDisplay and rememberNavWrapper logic
                        AshBikeMainScreen(
                            //viewModel = homeViewModel,
                            )
                    } else {
                        PermissionDeniedScreen(
                            onGoToSettings = { openAppSettings() },
                            onTryAgain = {
                                permissionAction.value = PermissionAction.CheckPermission
                            }
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun LocationPermissionRationaleDialog(
        onConfirm: () -> Unit,
        onDismiss: () -> Unit
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Location Permission Needed") },
            text = { Text("AshBike needs access to your location to track your rides and find coffee stops. Please grant the permission to continue.") },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text("Grant Permission")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Later")
                }
            }
        )
    }

    @Composable
    fun PermissionDeniedScreen(onGoToSettings: () -> Unit, onTryAgain: () -> Unit) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "Location permission is required.",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    "We cannot track your rides without it. Please enable it in Settings.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Button(onClick = onGoToSettings) {
                    Text("Open App Settings")
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onTryAgain) {
                    Text("Try Again")
                }
            }
        }
    }

    private fun openAppSettings() {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
            startActivity(this)
        }
    }
}