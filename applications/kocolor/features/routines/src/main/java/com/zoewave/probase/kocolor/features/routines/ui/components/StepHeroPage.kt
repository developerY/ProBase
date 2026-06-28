package com.zoewave.probase.kocolor.features.routines.ui.components

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zoewave.probase.core.model.ritual.CosmeticItem
import com.zoewave.probase.core.model.ritual.RoutineStep
import com.zoewave.probase.core.model.ritual.RoutineTime
import com.zoewave.probase.features.health.meals.ui.components.BioMealSuggestionSection
import com.zoewave.probase.kocolor.features.routines.R
import com.zoewave.probase.kocolor.features.routines.ui.RoutinesEvent
import com.zoewave.probase.kocolor.model.KoColorRoute
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class StepHeroUiState(
    val step: RoutineStep,
    val allProducts: List<CosmeticItem>,
    val routineId: Long,
    val routineTime: RoutineTime
)

@Composable
fun StepHeroPage(
    uiState: StepHeroUiState, 
    modifier: Modifier = Modifier,
    onEvent: (RoutinesEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val step = uiState.step
    val allProducts = uiState.allProducts
    val routineId = uiState.routineId
    val routineTime = uiState.routineTime
    
    val linkedProducts = allProducts.filter { step.productIds.contains(it.id) }
    
    var showJournalDialog by remember { mutableStateOf(false) }
    var journalDraft by remember { mutableStateOf("") }
    
    val dateFormatter = remember { 
        DateTimeFormatter.ofPattern("MMMM d • h:mm a").withZone(ZoneId.systemDefault()) 
    }

    // Default hero image
    val heroImageUrl = when (routineTime) {
        RoutineTime.MORNING -> R.drawable.morning_routine_bg
        RoutineTime.MEALS -> R.drawable.meals_ritual_bg
        else -> R.drawable.night_routine_bg
    }

    if (showJournalDialog) {
        AlertDialog(
            onDismissRequest = { showJournalDialog = false },
            title = { Text("New Journal Entry", fontFamily = FontFamily.Serif) },
            text = {
                OutlinedTextField(
                    value = journalDraft,
                    onValueChange = { journalDraft = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Add your notes...") },
                    minLines = 3
                )
            },
            confirmButton = {
                TextButton(onClick = { 
                    if (journalDraft.isNotBlank()) {
                        onEvent(RoutinesEvent.AddJournalEntry(routineId, step.id, journalDraft))
                        journalDraft = ""
                        showJournalDialog = false
                    }
                }) {
                    Text("ADD ENTRY", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showJournalDialog = false }) {
                    Text("CANCEL")
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize().background(Color(0xFFF9F7F2)), 
        contentPadding = PaddingValues(24.dp), 
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(bottom = 16.dp)) {
                Text(
                    text = "Ritual Stage Journaling", 
                    style = MaterialTheme.typography.displaySmall, 
                    fontFamily = FontFamily.Serif, 
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C2420)
                )
                Text(
                    text = "Context: Premium beauty and wellness app Atelier featuring personalized rituals.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color(0xFFF1F3F0))
            ) {
                val isMeal = routineTime == RoutineTime.MEALS
                AsyncImage(
                    model = if (linkedProducts.isNotEmpty()) linkedProducts.first().imageUrl else heroImageUrl, 
                    contentDescription = null, 
                    modifier = Modifier.fillMaxSize(), 
                    contentScale = ContentScale.Crop,
                    alpha = if (isMeal) 0.85f else 1.0f
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (isMeal) {
                                Brush.verticalGradient(
                                    listOf(Color.Black.copy(alpha = 0.2f), Color.Black.copy(alpha = 0.7f))
                                )
                            } else {
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f)),
                                    startY = 400f
                                )
                            }
                        )
                )
                Column(modifier = Modifier.align(Alignment.BottomStart).padding(32.dp)) {
                    Text(
                        text = "STAGE ${step.layeringOrder + 1}", 
                        style = MaterialTheme.typography.labelSmall, 
                        color = Color.White.copy(alpha = 0.8f), 
                        fontWeight = FontWeight.Black, 
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = step.title, 
                        style = MaterialTheme.typography.displayMedium, 
                        color = Color.White, 
                        fontFamily = FontFamily.Serif, 
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        val subtitle = step.subtitle
        if (subtitle != null) {
            item {
                Text(
                    text = subtitle.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 2.sp
                )
            }
        }

        if (step.description.isNotBlank()) {
            item {
                Text(
                    text = step.description,
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 28.sp,
                    fontFamily = FontFamily.Serif,
                    color = Color(0xFF1A1A1A)
                )
            }
        }

        if (routineTime == RoutineTime.MEALS) {
            item {
                BioMealSuggestionSection(
                    currentMealId = step.linkedMealId,
                    onMealSelected = { meal ->
                        onEvent(RoutinesEvent.LinkMeal(routineId, step.id, meal.id))
                        navTo(KoColorRoute.MealsHub(mealId = meal.id, isCooking = false))
                    }
                )
            }
            
            item {
                Surface(
                    color = Color(0xFFE0C097).copy(alpha = 0.05f),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0C097).copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = step.actionLabel ?: "Meal suggestion pending...",
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily.Serif,
                            lineHeight = 22.sp
                        )
                    }
                }
            }
        } else {
            val actionLabel = step.actionLabel
            if (actionLabel != null) {
                item {
                    Surface(
                        color = Color.White.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(24.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
                    ) {
                        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "YOUR ACTION",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = Color.Gray,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = actionLabel,
                                style = MaterialTheme.typography.bodyMedium,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                lineHeight = 24.sp,
                                fontFamily = FontFamily.Serif
                            )
                        }
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "PROGRESS PHOTOS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = Color.Gray,
                    letterSpacing = 1.sp
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(step.photoUris) { uri ->
                        Box {
                            AsyncImage(
                                model = uri,
                                contentDescription = null,
                                modifier = Modifier.size(140.dp).clip(RoundedCornerShape(20.dp)),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = { onEvent(RoutinesEvent.RemoveStepPhoto(routineId, step.id, uri)) },
                                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).size(28.dp).background(Color.Black.copy(alpha = 0.3f), CircleShape)
                            ) {
                                Icon(Icons.Rounded.Delete, null, tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                    
                    item {
                        Surface(
                            onClick = { navTo(KoColorRoute.Camera("ritual_step:$routineId:${step.id}")) },
                            modifier = Modifier.size(100.dp),
                            shape = CircleShape,
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f)),
                            shadowElevation = 4.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.AddAPhoto, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                            }
                        }
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RITUAL JOURNAL & HISTORY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = Color.Gray,
                        letterSpacing = 1.sp
                    )
                }

                Button(
                    onClick = { showJournalDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.Black),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Add New Journal Entry", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }

                if (step.journalEntries.isEmpty()) {
                    Text("No journal entries yet.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray.copy(alpha = 0.6f))
                } else {
                    step.journalEntries.forEach { entry ->
                        Surface(
                            color = Color(0xFFF3E5F5), 
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = dateFormatter.format(Instant.ofEpochMilli(entry.timestamp)),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray
                                    )
                                    IconButton(
                                        onClick = { onEvent(RoutinesEvent.DeleteJournalEntry(routineId, step.id, entry.id)) },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(Icons.Rounded.Delete, null, tint = Color.Gray.copy(alpha = 0.4f), modifier = Modifier.size(14.dp))
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = entry.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = FontFamily.Serif,
                                    lineHeight = 22.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        if (linkedProducts.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    Text(
                        text = "LINKED INVENTORY ITEMS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = Color.Gray,
                        letterSpacing = 1.sp
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(linkedProducts) { product ->
                            Card(
                                modifier = Modifier.width(180.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
                            ) {
                                Column {
                                    AsyncImage(
                                        model = product.imageUrl,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxWidth().height(160.dp),
                                        contentScale = ContentScale.Crop
                                    )
                                    Text(
                                        text = product.name,
                                        modifier = Modifier.padding(12.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Serif,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(48.dp)) }
    }
}

@Preview(showBackground = true)
@Composable
private fun StepHeroPagePreview() {
    MaterialTheme {
        StepHeroPage(uiState = StepHeroUiState(RoutineStep(id = "1", title = "Step", layeringOrder = 0), emptyList(), 1L, RoutineTime.MORNING), onEvent = {}, navTo = {})
    }
}
