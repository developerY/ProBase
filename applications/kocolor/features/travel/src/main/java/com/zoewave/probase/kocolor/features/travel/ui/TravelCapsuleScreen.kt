package com.zoewave.probase.kocolor.features.travel.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelCapsuleScreen(
    uiState: TravelCapsuleUiState,
    onEvent: (TravelEvent) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Destination Capsule", fontFamily = FontFamily.Serif) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        // Normally back icon
                        Icon(Icons.Default.Flight, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (!uiState.isGenerated) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Where are we going?",
                    style = MaterialTheme.typography.headlineMedium,
                    fontFamily = FontFamily.Serif
                )

                OutlinedTextField(
                    value = uiState.destination,
                    onValueChange = { onEvent(TravelEvent.UpdateDestination(it)) },
                    label = { Text("Destination (e.g. Paris)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = uiState.occasion,
                    onValueChange = { onEvent(TravelEvent.UpdateOccasion(it)) },
                    label = { Text("Occasion (e.g. Business Trip, Skiing)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { onEvent(TravelEvent.GenerateCapsule) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Luggage, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Pack My Bags")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        "Your Capsule for ${uiState.destination}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontFamily = FontFamily.Serif
                    )
                    Text("Weather expected: ${uiState.weatherForecast}", color = Color.Gray)
                }

                if (uiState.passportReminder) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFF856404))
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    "International Trip Detected! Don't forget your passport and check visa requirements.",
                                    color = Color(0xFF856404)
                                )
                            }
                        }
                    }
                }

                item {
                    Surface(color = Color(0xFFF3F4F6), shape = RoundedCornerShape(12.dp)) {
                        Text(
                            uiState.rationale,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                item {
                    Text("Wardrobe", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }

                items(uiState.recommendedClothing) { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color.White).padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = item.imageUrl,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(item.name, fontWeight = FontWeight.Bold)
                            Text(item.category.name, color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }

                item {
                    Text("Vanity & Cosmetics", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }

                items(uiState.recommendedCosmetics) { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color.White).padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = item.imageUrl,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(item.name, fontWeight = FontWeight.Bold)
                            Text(item.macroCategory.name, color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }

                item {
                    Button(
                        onClick = { onEvent(TravelEvent.Reset) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray, contentColor = Color.Black)
                    ) {
                        Text("Plan Another Trip")
                    }
                }
            }
        }
    }
}
