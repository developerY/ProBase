package com.zoewave.probase.features.health.meals.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zoewave.probase.features.health.meals.data.Meal
import com.zoewave.probase.features.health.meals.data.MealStep
import com.zoewave.probase.features.health.meals.ui.BioOptimizedColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPreparationScreen(
    meal: Meal,
    currentStepIndex: Int,
    onStepClick: (Int) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meal Preparation", color = BioOptimizedColors.Cyan400, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BioOptimizedColors.Slate950)
            )
        },
        containerColor = BioOptimizedColors.Slate950
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header Image
            if (meal.imageUrl != null) {
                AsyncImage(
                    model = meal.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentScale = ContentScale.Crop
                )
            }
            
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = meal.name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "15 min prep", // Placeholder as per design
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94a3b8)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Preparation Steps",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
            ) {
                itemsIndexed(meal.steps) { index, step ->
                    PreparationStepItem(
                        step = step,
                        isCompleted = index < currentStepIndex,
                        isActive = index == currentStepIndex,
                        isLast = index == meal.steps.size - 1,
                        onClick = { onStepClick(index) }
                    )
                }
            }

            // Bottom Navigation
            Surface(
                color = BioOptimizedColors.Slate900,
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 16.dp
            ) {
                Row(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = onPrevious,
                        modifier = Modifier.weight(1f).height(56.dp),
                        enabled = currentStepIndex > 0,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF)),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Text("Previous")
                    }
                    
                    Button(
                        onClick = onNext,
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BioOptimizedColors.Cyan400,
                            contentColor = BioOptimizedColors.Slate950
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Text(if (currentStepIndex == meal.steps.size - 1) "Finish" else "Next Step", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun PreparationStepItem(
    step: MealStep,
    isCompleted: Boolean,
    isActive: Boolean,
    isLast: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Step Indicator Column
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(40.dp)
        ) {
            Surface(
                modifier = Modifier.size(32.dp),
                shape = CircleShape,
                color = when {
                    isCompleted -> BioOptimizedColors.Cyan400
                    isActive -> BioOptimizedColors.Slate800
                    else -> Color.Transparent
                },
                border = if (!isCompleted && !isActive) androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF)) else null
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isCompleted) {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp), tint = BioOptimizedColors.Slate950)
                    } else {
                        Text(
                            text = "${step.order}",
                            color = if (isActive) BioOptimizedColors.Cyan400 else Color(0xFF94a3b8),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(80.dp)
                        .background(if (isCompleted) BioOptimizedColors.Cyan400 else Color(0x33FFFFFF))
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Step Content Card
        Card(
            modifier = Modifier
                .weight(1f)
                .alpha(if (isCompleted || isActive) 1f else 0.5f),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isActive) Color(0x1AFFFFFF) else Color.Transparent
            ),
            border = if (isActive) androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF)) else null
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Step ${step.order}",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isActive) BioOptimizedColors.Cyan400 else Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    if (isActive) {
                        Spacer(modifier = Modifier.weight(1f))
                        Surface(
                            color = BioOptimizedColors.Pink400.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "In Progress",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = BioOptimizedColors.Pink400
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = step.instruction,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isActive) Color.White else Color(0xFFcbd5e1)
                )
            }
        }
    }
}
