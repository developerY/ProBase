package com.zoewave.probase.rxlogic.features.daily

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.rxlogic.model.Frequency
import com.zoewave.probase.rxlogic.model.navigation.RxLogicRoute
import kotlinx.datetime.LocalTime

@Composable
fun DailyScreen(
    uiState: DailyUiState,
    onEvent: (DailyEvent) -> Unit,
    navTo: (RxLogicRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Schedule",
                    style = MaterialTheme.typography.headlineMedium
                )
                IconButton(onClick = { /* History */ }) {
                    Icon(Icons.Default.History, contentDescription = "History")
                }
            }
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val groupedMeds = uiState.medications.groupBy { 
                when (it.scheduledTime.hour) {
                    in 5..11 -> "Morning"
                    in 12..16 -> "Afternoon"
                    in 17..20 -> "Evening"
                    else -> "Night"
                }
            }

            LazyColumn(
                modifier = Modifier.padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    WeeklyAdherenceStrip(uiState.weeklyAdherence)
                }

                item {
                    DailyProgressCard(stats = uiState.stats)
                }

                if (uiState.medications.any { it.status == DailyTaskStatus.UPCOMING }) {
                    item {
                        val nextTask = uiState.medications.first { it.status == DailyTaskStatus.UPCOMING }
                        NextDoseCard(task = nextTask)
                    }
                }

                if (uiState.medications.isEmpty()) {
                    item {
                        EmptyStateCard(onAddClick = { navTo(RxLogicRoute.Medications) })
                    }
                } else {
                    listOf("Morning", "Afternoon", "Evening", "Night").forEach { period ->
                        groupedMeds[period]?.let { periodMeds ->
                            item {
                                Text(
                                    text = period,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                            items(periodMeds) { task ->
                                DailyMedicationItem(
                                    task = task,
                                    onTakenClick = { onEvent(DailyEvent.OnMarkAsTaken(task.medicationId)) }
                                )
                            }
                        }
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun WeeklyAdherenceStrip(adherence: List<AdherenceDay>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Last 7 Days", style = MaterialTheme.typography.labelMedium)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                adherence.forEach { day ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = day.date.dayOfMonth.toString(),
                            style = MaterialTheme.typography.labelSmall
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(
                                    if (day.totalMeds == 0) MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                    else if (day.isFullyTaken) Color(0xFF4CAF50)
                                    else if (day.takenMeds > 0) Color(0xFFFFC107)
                                    else MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (day.isFullyTaken) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            } else if (day.totalMeds > 0) {
                                Text(
                                    text = "${day.takenMeds}/${day.totalMeds}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateCard(onAddClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No medications scheduled for today.",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Start by adding your medications in the Medications tab.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        androidx.compose.material3.Button(onClick = onAddClick) {
            Text("Add Medications")
        }
    }
}

@Composable
fun NextDoseCard(task: DailyMedicationTask) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsActive,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary
            )
            Column {
                Text(
                    text = "Next Dose: ${task.scheduledTime.toString().take(5)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.tertiary
                )
                Text(
                    text = "${task.name} (${task.dosage})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun DailyProgressCard(stats: DailyStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Daily Adherence",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            val percentage = if (stats.totalMeds > 0) (stats.takenMeds * 100 / stats.totalMeds) else 0
            Text(
                text = "$percentage% of your medications taken today",
                style = MaterialTheme.typography.bodyMedium
            )
            val progress = if (stats.totalMeds > 0) stats.takenMeds.toFloat() / stats.totalMeds else 0f
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .height(8.dp)
                    .clip(MaterialTheme.shapes.small),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
            )
            Text(
                text = "${stats.takenMeds} / ${stats.totalMeds} completed",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
fun DailyMedicationItem(
    task: DailyMedicationTask,
    onTakenClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (task.status) {
                DailyTaskStatus.TAKEN -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                DailyTaskStatus.MISSED -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(60.dp)) {
                val timeString = task.scheduledTime.toString().take(5)
                Text(
                    text = timeString,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (task.scheduledTime.hour < 12) "AM" else "PM",
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (task.status == DailyTaskStatus.TAKEN) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                )
                Text(
                    text = task.dosage,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                task.instructions?.let { 
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                if (task.status == DailyTaskStatus.MISSED) {
                    Text(
                        text = "Missed",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            IconButton(
                onClick = onTakenClick,
                enabled = task.status == DailyTaskStatus.UPCOMING || task.status == DailyTaskStatus.MISSED
            ) {
                Icon(
                    imageVector = if (task.status == DailyTaskStatus.TAKEN) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Mark as taken",
                    tint = if (task.status == DailyTaskStatus.TAKEN) Color(0xFF4CAF50) else if (task.status == DailyTaskStatus.MISSED) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DailyScreenPreview() {
    DailyScreen(
        uiState = DailyUiState(
            medications = listOf(
                DailyMedicationTask(
                    medicationId = "1",
                    name = "Aspirin",
                    dosage = "100mg",
                    scheduledTime = LocalTime(8, 0),
                    status = DailyTaskStatus.TAKEN,
                    frequency = Frequency.DAILY
                ),
                DailyMedicationTask(
                    medicationId = "2",
                    name = "Vitamin D",
                    dosage = "1000IU",
                    scheduledTime = LocalTime(12, 0),
                    status = DailyTaskStatus.UPCOMING,
                    frequency = Frequency.DAILY
                ),
                DailyMedicationTask(
                    medicationId = "3",
                    name = "Magnesium",
                    dosage = "200mg",
                    scheduledTime = LocalTime(20, 0),
                    status = DailyTaskStatus.UPCOMING,
                    frequency = Frequency.DAILY
                )
            ),
            stats = DailyStats(totalMeds = 3, takenMeds = 1)
        ),
        onEvent = {},
        navTo = {}
    )
}
