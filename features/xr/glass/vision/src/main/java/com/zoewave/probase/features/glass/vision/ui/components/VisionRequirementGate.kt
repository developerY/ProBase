package com.zoewave.probase.features.glass.vision.ui.components

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.zoewave.probase.features.glass.vision.ui.VisionViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun VisionRequirementGate(
    viewModel: VisionViewModel,
    onNavigateToSettings: () -> Unit,
    onRequestGlassesPermission: () -> Unit,
    content: @Composable () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    val isPhonePermissionOk = uiState.isPermissionGranted
    val isGlassesPermissionOk = uiState.isGlassesPermissionGranted
    val isApiKeyOk = uiState.isApiKeySet

    if (isPhonePermissionOk && isGlassesPermissionOk && isApiKeyOk) {
        content()
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Setup Required",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Complete the following steps to use Vision AI",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(Modifier.height(32.dp))

            RequirementCard(
                title = "Phone Camera Access",
                description = "Required to allow projected access.",
                isMet = isPhonePermissionOk,
                icon = Icons.Default.CameraAlt,
                buttonLabel = "GRANT PHONE ACCESS",
                onButtonClick = { cameraPermissionState.launchPermissionRequest() }
            )

            Spacer(Modifier.height(16.dp))

            RequirementCard(
                title = "Glasses Camera Access",
                description = "Allow the app to use the glasses hardware.",
                isMet = isGlassesPermissionOk,
                icon = Icons.Default.CameraAlt,
                buttonLabel = "GRANT GLASSES ACCESS",
                onButtonClick = onRequestGlassesPermission
            )

            Spacer(Modifier.height(16.dp))

            RequirementCard(
                title = "Gemini API Key",
                description = "Configure your Gemini API key in settings.",
                isMet = isApiKeyOk,
                icon = Icons.Default.VpnKey,
                buttonLabel = "GO TO SETTINGS",
                onButtonClick = onNavigateToSettings
            )
        }
    }
}

@Composable
private fun RequirementCard(
    title: String,
    description: String,
    isMet: Boolean,
    icon: ImageVector,
    buttonLabel: String,
    onButtonClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isMet) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (isMet) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isMet) Icons.Default.CheckCircle else icon,
                    contentDescription = null,
                    tint = Color.White
                )
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = description, style = MaterialTheme.typography.bodySmall)
                
                if (!isMet) {
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = onButtonClick,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(buttonLabel, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}
