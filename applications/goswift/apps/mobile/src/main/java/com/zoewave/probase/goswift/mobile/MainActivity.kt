package com.zoewave.probase.goswift.mobile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.lifecycleScope
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.zoewave.probase.core.data.service.health.HealthSessionManager
import com.zoewave.probase.goswift.mobile.ui.components.GoSwiftMainScreen
import com.zoewave.probase.goswift.mobile.ui.theme.GoSwiftTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var healthSessionManager: HealthSessionManager

    private val permissions = setOf(
        "android.permission.health.READ_SLEEP",
        "android.permission.health.READ_EXERCISE"
    )

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Set<String>>

    private var permissionStatus by mutableStateOf<PermissionStatus>(PermissionStatus.Checking)

    sealed class PermissionStatus {
        data object Checking : PermissionStatus()
        data object Granted : PermissionStatus()
        data object Denied : PermissionStatus()
        data object ShowRationale : PermissionStatus()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize launcher AFTER lateinit injection but within onCreate
        requestPermissionLauncher = registerForActivityResult(
            PermissionController.createRequestPermissionResultContract()
        ) { granted ->
            if (granted.containsAll(permissions)) {
                Log.d("MainActivity", "Health Permissions Granted")
                permissionStatus = PermissionStatus.Granted
            } else {
                Log.d("MainActivity", "Health Permissions Denied")
                permissionStatus = PermissionStatus.Denied
            }
        }

        val firebaseAnalytics = Firebase.analytics
        firebaseAnalytics.setUserProperty("device_platform", "mobile")
        firebaseAnalytics.logEvent("app_open", null)
        enableEdgeToEdge()

        checkPermissions()

        setContent {
            GoSwiftTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (val status = permissionStatus) {
                        PermissionStatus.Checking -> {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Checking Permissions...")
                            }
                        }
                        PermissionStatus.Granted -> {
                            GoSwiftMainScreen()
                        }
                        PermissionStatus.Denied -> {
                            PermissionDeniedScreen(
                                onGoToSettings = { openAppSettings() },
                                onTryAgain = { checkPermissions() }
                            )
                        }
                        PermissionStatus.ShowRationale -> {
                            HealthPermissionRationaleDialog(
                                onConfirm = {
                                    requestPermissionLauncher.launch(permissions)
                                },
                                onDismiss = {
                                    permissionStatus = PermissionStatus.Denied
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun checkPermissions() {
        lifecycleScope.launch {
            if (healthSessionManager.hasAllPermissions(permissions)) {
                permissionStatus = PermissionStatus.Granted
            } else {
                permissionStatus = PermissionStatus.ShowRationale
            }
        }
    }

    @Composable
    fun HealthPermissionRationaleDialog(
        onConfirm: () -> Unit,
        onDismiss: () -> Unit
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Health Data Needed") },
            text = { Text("GoSwift needs access to your sleep and exercise data from Health Connect to provide personalized caffeine recommendations.") },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text("Grant Permissions")
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
                    "Permissions Required",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    "Without Health Connect permissions, we cannot correlate your caffeine intake with your sleep and exercise activity.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Button(onClick = onGoToSettings) {
                    Text("Open Settings")
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onTryAgain) {
                    Text("Try Again")
                }
            }
        }
    }

    private fun openAppSettings() {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.healthdata")
            setPackage("com.android.vending")
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            // Fallback to app details
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
                startActivity(this)
            }
        }
    }
}
