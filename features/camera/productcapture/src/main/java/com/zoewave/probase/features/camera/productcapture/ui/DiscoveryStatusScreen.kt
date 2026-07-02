package com.zoewave.probase.features.camera.productcapture.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zoewave.probase.core.model.network.DiscoveryStatus
import com.zoewave.probase.core.model.network.ServiceStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoveryStatusScreen(
    status: DiscoveryStatus,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val services = listOf(
        ServiceInfo("Open Beauty Facts", "Barcode Lookup", status.obf),
        ServiceInfo("OpenFDA", "Safety & Recalls", status.fda),
        ServiceInfo("chemDB (PubChem)", "Ingredient Hazards", status.chemDb),
        ServiceInfo("The Color API", "Palette & Naming", status.colorApi),
        ServiceInfo("Google Gemini", "Multimodal Analysis", status.gemini),
        ServiceInfo("Makeup API", "Discovery & Filtering", status.makeupApi)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Discovery Health", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            )
        },
        containerColor = Color(0xFF0f172a)
    ) { padding ->
        Column(
            modifier = modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Text(
                "Service Connectivity",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Visualizing real-time server access as the engine works",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f)
            )

            Spacer(Modifier.height(32.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(services) { service ->
                    ServiceStatusRow(service)
                }
            }
        }
    }
}

private data class ServiceInfo(
    val name: String,
    val description: String,
    val status: ServiceStatus
)

@Composable
private fun ServiceStatusRow(service: ServiceInfo) {
    val backgroundColor by animateColorAsState(
        when (service.status) {
            ServiceStatus.SUCCESS -> Color(0xFF065f46)
            ServiceStatus.FAILED -> Color(0xFF7f1d1d)
            ServiceStatus.ACCESSING -> Color(0xFF1e293b)
            ServiceStatus.IDLE -> Color(0xFF1e293b).copy(alpha = 0.5f)
        },
        label = "backgroundColor"
    )

    val iconColor = when (service.status) {
        ServiceStatus.SUCCESS -> Color(0xFF34d399)
        ServiceStatus.FAILED -> Color(0xFFf87171)
        ServiceStatus.ACCESSING -> Color(0xFF60a5fa)
        ServiceStatus.IDLE -> Color.Gray
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconColor.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                when (service.status) {
                    ServiceStatus.SUCCESS -> Icon(Icons.Default.Check, null, tint = iconColor)
                    ServiceStatus.FAILED -> Icon(Icons.Default.Close, null, tint = iconColor)
                    ServiceStatus.ACCESSING -> {
                        val rotation by animateFloatAsState(
                            targetValue = 360f,
                            animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                                animation = androidx.compose.animation.core.tween(1000)
                            ),
                            label = "rotation"
                        )
                        Icon(Icons.Default.Sync, null, tint = iconColor, modifier = Modifier.rotate(rotation))
                    }
                    ServiceStatus.IDLE -> Box(modifier = Modifier.size(8.dp).background(Color.Gray, CircleShape))
                }
            }

            Spacer(Modifier.width(16.dp))

            Column {
                Text(
                    service.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    service.description,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}
