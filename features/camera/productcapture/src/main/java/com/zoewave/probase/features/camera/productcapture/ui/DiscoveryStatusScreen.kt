package com.zoewave.probase.features.camera.productcapture.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zoewave.probase.core.model.network.DiscoveryStatus
import com.zoewave.probase.core.model.network.ServiceHealth
import com.zoewave.probase.core.model.network.ServiceStatus

enum class DiscoveryMode {
    DETERMINISTIC, AI_SYNTHESIS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoveryStatusScreen(
    status: DiscoveryStatus,
    mode: DiscoveryMode = DiscoveryMode.DETERMINISTIC,
    onBack: () -> Unit = {},
    onNext: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val services = when (mode) {
        DiscoveryMode.DETERMINISTIC -> listOf(
            ServiceInfo("Open Beauty Facts", "Barcode Lookup", status.obf),
            ServiceInfo("OpenFDA", "Safety & Recalls", status.fda),
            ServiceInfo("chemDB (PubChem)", "Ingredient Hazards", status.chemDb),
            ServiceInfo("Makeup API", "Discovery & Filtering", status.makeupApi)
        )
        DiscoveryMode.AI_SYNTHESIS -> listOf(
            ServiceInfo("Google Gemini", "Multimodal Analysis", status.gemini),
            ServiceInfo("The Color API", "Palette & Naming", status.colorApi)
        )
    }

    // Check if all services in current mode have finished (Success or Failed)
    val allFinished = services.all { 
        it.health.status == ServiceStatus.SUCCESS || it.health.status == ServiceStatus.FAILED 
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0f172a).copy(alpha = 0.95f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        if (mode == DiscoveryMode.DETERMINISTIC) "Discovery Engine" else "AI Synthesis Engine",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val activeColor = if (allFinished) Color(0xFF34d399) else Color(0xFF60a5fa)
                        
                        if (!allFinished) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp,
                                color = activeColor
                            )
                        } else {
                            Box(modifier = Modifier.size(8.dp).background(activeColor, CircleShape))
                        }
                        
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (allFinished) "PROCESSING COMPLETE" else "ENGINE ACTIVE",
                            style = MaterialTheme.typography.labelSmall,
                            color = activeColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            Spacer(Modifier.height(32.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(services) { service ->
                    ServiceStatusRow(service)
                }
            }
            
            Spacer(Modifier.height(16.dp))

            if (onNext != null && allFinished) {
                Button(
                    onClick = onNext,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF34d399),
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        if (mode == DiscoveryMode.DETERMINISTIC) "CONTINUE TO REVIEW" else "FINALIZE PRODUCT",
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(12.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null)
                }
                Spacer(Modifier.height(16.dp))
            }
            
            Text(
                if (allFinished) "Processing complete. All systems stable."
                else "KoColor boutique engine is online and processing your request.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.4f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private data class ServiceInfo(
    val name: String,
    val description: String,
    val health: ServiceHealth
)

@Composable
private fun ServiceStatusRow(service: ServiceInfo) {
    val backgroundColor by animateColorAsState(
        when (service.health.status) {
            ServiceStatus.SUCCESS -> Color(0xFF065f46)
            ServiceStatus.FAILED -> Color(0xFF7f1d1d)
            ServiceStatus.ACCESSING -> Color(0xFF1e293b)
            ServiceStatus.IDLE -> Color(0xFF1e293b).copy(alpha = 0.5f)
        },
        label = "backgroundColor"
    )

    val iconColor = when (service.health.status) {
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
                when (service.health.status) {
                    ServiceStatus.SUCCESS -> {
                        Surface(
                            shape = CircleShape,
                            color = Color.Transparent,
                            border = androidx.compose.foundation.BorderStroke(2.dp, iconColor),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Check, null, tint = iconColor, modifier = Modifier.padding(4.dp))
                        }
                    }
                    ServiceStatus.FAILED -> {
                        Surface(
                            shape = CircleShape,
                            color = Color.Transparent,
                            border = androidx.compose.foundation.BorderStroke(2.dp, iconColor),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Close, null, tint = iconColor, modifier = Modifier.padding(4.dp))
                        }
                    }
                    ServiceStatus.ACCESSING -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = iconColor
                        )
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
                    service.health.note ?: service.description,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}
