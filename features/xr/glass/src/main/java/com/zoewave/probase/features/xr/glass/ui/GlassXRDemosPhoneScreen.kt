package com.zoewave.probase.features.xr.glass.ui

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.CastConnected
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.xr.projected.ProjectedContext
import androidx.xr.projected.experimental.ExperimentalProjectedApi
import com.zoewave.probase.features.xr.glass.GlassesMainActivity

@OptIn(ExperimentalMaterial3Api::class, ExperimentalProjectedApi::class)
@Composable
fun GlassXRDemosPhoneScreen(
    onBack: () -> Unit,
    viewModel: GlassXRDemosViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isConnected by viewModel.isConnected.collectAsStateWithLifecycle()
    val activeSample by viewModel.activeSample.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.checkConnection(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Glass XR Demos") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isConnected) {
                        Icon(
                            imageVector = Icons.Default.CastConnected,
                            contentDescription = "Glasses Connected",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isConnected) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.secondaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (isConnected) "Glasses Connected" else "Projected Experience",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isConnected) Color(0xFF2E7D32) else Color.Unspecified
                        )
                        Text(
                            text = if (isConnected) 
                                "Your glasses are ready. Tap a sample below to project it immediately." 
                                else "These samples are designed to be projected onto intelligent eyewear. Connect your glasses and tap 'Launch' to see the Glimmer UI in action.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            items(GlimmerSample.entries) { sample ->
                val isActive = activeSample == sample
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    border = if (isActive) CardDefaults.outlinedCardBorder().copy(width = 2.dp, brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF4CAF50))) else CardDefaults.outlinedCardBorder(),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = if (isActive) Color(0xFFE8F5E9) else Color.Transparent
                    ),
                    onClick = {
                        launchOnGlasses(context, sample)
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = sample.title, 
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isActive) androidx.compose.ui.text.font.FontWeight.Bold else null
                            )
                            Text(
                                "Demo of Glimmer ${sample.title} component.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = if (isActive) Icons.Default.CastConnected else Icons.Default.Cast,
                            contentDescription = "Launch on Glasses",
                            tint = if (isConnected || isActive) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalProjectedApi::class)
private fun launchOnGlasses(context: android.content.Context, sample: GlimmerSample) {
    if (android.os.Build.VERSION.SDK_INT >= 35) {
        try {
            val options = ProjectedContext.createProjectedActivityOptions(context)
            val intent = Intent(context, GlassesMainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra("initial_sample", sample.name)
            }
            context.startActivity(intent, options.toBundle())
        } catch (e: Exception) {
            val intent = Intent(context, GlassesMainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra("initial_sample", sample.name)
            }
            context.startActivity(intent)
        }
    } else {
        val intent = Intent(context, GlassesMainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra("initial_sample", sample.name)
        }
        context.startActivity(intent)
    }
}
