package com.zoewave.probase.kocolor.features.travel.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
            ConfigurationScreen(uiState, onEvent, padding)
        } else {
            ChecklistScreen(uiState, onEvent, padding)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfigurationScreen(
    uiState: TravelCapsuleUiState,
    onEvent: (TravelEvent) -> Unit,
    padding: PaddingValues
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

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

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedCard(
                modifier = Modifier.weight(1f),
                onClick = { onEvent(TravelEvent.ToggleStartDatePicker(true)) }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Start Date", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(
                        text = uiState.startDateMillis?.let { dateFormat.format(Date(it)) } ?: "Select",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            OutlinedCard(
                modifier = Modifier.weight(1f),
                onClick = { onEvent(TravelEvent.ToggleEndDatePicker(true)) }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("End Date", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(
                        text = uiState.endDateMillis?.let { dateFormat.format(Date(it)) } ?: "Select",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (uiState.showStartDatePicker) {
            val datePickerState = rememberDatePickerState(initialSelectedDateMillis = uiState.startDateMillis)
            DatePickerDialog(
                onDismissRequest = { onEvent(TravelEvent.ToggleStartDatePicker(false)) },
                confirmButton = {
                    TextButton(onClick = { onEvent(TravelEvent.UpdateStartDate(datePickerState.selectedDateMillis)) }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { onEvent(TravelEvent.ToggleStartDatePicker(false)) }) { Text("Cancel") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        if (uiState.showEndDatePicker) {
            val datePickerState = rememberDatePickerState(initialSelectedDateMillis = uiState.endDateMillis)
            DatePickerDialog(
                onDismissRequest = { onEvent(TravelEvent.ToggleEndDatePicker(false)) },
                confirmButton = {
                    TextButton(onClick = { onEvent(TravelEvent.UpdateEndDate(datePickerState.selectedDateMillis)) }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { onEvent(TravelEvent.ToggleEndDatePicker(false)) }) { Text("Cancel") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

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
}

@Composable
private fun ChecklistScreen(
    uiState: TravelCapsuleUiState,
    onEvent: (TravelEvent) -> Unit,
    padding: PaddingValues
) {
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

        val groupedItems = uiState.checklist.groupBy { it.category }

        PackingCategory.entries.forEach { category ->
            val itemsInCategory = groupedItems[category]
            if (!itemsInCategory.isNullOrEmpty()) {
                item {
                    Text(
                        text = category.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }

                itemsInCategory.forEach { item ->
                    item {
                        PackingListItemRow(
                            item = item,
                            onToggle = { onEvent(TravelEvent.ToggleItemCheck(item.id)) }
                        )
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(24.dp))
            if (uiState.isSaved) {
                Button(
                    onClick = {},
                    enabled = false,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(disabledContainerColor = Color(0xFFDCFCE7), disabledContentColor = Color(0xFF166534))
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Trip Saved to Collection", fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = { onEvent(TravelEvent.SaveTrip) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5), contentColor = Color.White)
                ) {
                    Icon(Icons.Default.Bookmark, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Save Trip to Collection", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = { onEvent(TravelEvent.Reset) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Plan Another Trip")
            }
        }
    }
}

@Composable
private fun PackingListItemRow(item: PackingChecklistItem, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (item.isChecked) Color(0xFFF0FDF4) else Color.White)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = item.isChecked,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF22C55E))
        )

        Spacer(Modifier.width(8.dp))

        if (item.imageUrl != null) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp))
            )
        } else {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE5E7EB)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (item.isOwned) Icons.Default.Luggage else Icons.Default.AddShoppingCart,
                    contentDescription = null,
                    tint = Color.Gray
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None,
                color = if (item.isChecked) Color.Gray else Color.Black
            )
            val subText = if (!item.isOwned) "To Buy/Pack" else if (item.quantity > 1) "Pack ${item.quantity}" else "Owned"
            val subColor = if (!item.isOwned) Color(0xFFEF4444) else Color.Gray
            Text(subText, color = subColor, fontSize = 12.sp, fontWeight = if (!item.isOwned) FontWeight.Bold else FontWeight.Normal)
        }
    }
}
