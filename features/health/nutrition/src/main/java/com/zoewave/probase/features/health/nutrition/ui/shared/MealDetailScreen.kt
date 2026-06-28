package com.zoewave.probase.features.health.nutrition.ui.shared

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zoewave.probase.features.health.nutrition.R
import com.zoewave.probase.features.health.nutrition.data.Ingredient
import com.zoewave.probase.features.health.nutrition.data.Meal
import com.zoewave.probase.features.health.nutrition.data.MealStep
import com.zoewave.probase.features.health.nutrition.data.MetabolicPhase
import com.zoewave.probase.features.health.nutrition.data.NutritionInfo
import com.zoewave.probase.features.health.nutrition.ui.shared.BioOptimizedColors

@Preview(showBackground = true)
@Composable
private fun MealDetailScreenPreview() {
    MaterialTheme {
        MealDetailScreen(
            meal = Meal(
                id = "1",
                name = "Golden Turmeric Elixir",
                description = "Anti-inflammatory morning tonic to kickstart mTOR and cellular signaling.",
                scientificFocus = "Curcumin Bioavailability",
                phase = MetabolicPhase.Morning,
                nutrition = NutritionInfo(
                    calories = 120,
                    protein = 2f,
                    carbs = 15f,
                    fat = 5f
                ),
                ingredients = listOf(
                    Ingredient("Turmeric", "1 tsp"),
                    Ingredient("Black Pepper", "pinch"),
                    Ingredient("Coconut Milk", "200ml")
                ),
                steps = listOf(
                    MealStep(1, "Warm the coconut milk."),
                    MealStep(2, "Whisk in turmeric and pepper.")
                )
            ),
            onEdit = {},
            onDelete = {},
            onStartCooking = {},
            onBack = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealDetailScreen(
    meal: Meal,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onStartCooking: () -> Unit,
    onBack: () -> Unit
) {
    val accentColor = when (meal.phase) {
        MetabolicPhase.Morning -> BioOptimizedColors.Lime400
        MetabolicPhase.MidDay -> BioOptimizedColors.Cyan400
        MetabolicPhase.Evening -> BioOptimizedColors.Pink400
    }

    var showDeleteConfirmation by remember { mutableStateOf(false) }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Meal?", color = Color.White) },
            text = { Text("Are you sure you want to remove this metabolic protocol from your history?", color = Color(0xFFcbd5e1)) },
            confirmButton = {
                TextButton(onClick = { 
                    showDeleteConfirmation = false
                    onDelete() 
                }) {
                    Text(stringResource(R.string.features_health_nutrition_action_delete), color = BioOptimizedColors.Pink400, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text(stringResource(R.string.features_health_nutrition_action_cancel), color = Color.White)
                }
            },
            containerColor = BioOptimizedColors.Slate900,
            shape = RoundedCornerShape(24.dp)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(meal.name, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
                    }
                    IconButton(onClick = { showDeleteConfirmation = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White.copy(alpha = 0.6f))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BioOptimizedColors.Slate950)
            )
        },
        containerColor = BioOptimizedColors.Slate950
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item {
                if (meal.imageUrl != null) {
                    AsyncImage(
                        model = meal.imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(
                                Brush.verticalGradient(
                                    listOf(BioOptimizedColors.Slate900, BioOptimizedColors.Slate950)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (meal.phase) {
                                MetabolicPhase.Morning -> "🍳"
                                MetabolicPhase.MidDay -> "🥗"
                                MetabolicPhase.Evening -> "🍠"
                            },
                            fontSize = 64.sp
                        )
                    }
                }
                
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(accentColor, RoundedCornerShape(50))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = meal.phase.name.uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = accentColor,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.features_health_nutrition_label_scientific_focus),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF94a3b8)
                    )
                    Text(
                        text = meal.scientificFocus,
                        style = MaterialTheme.typography.titleLarge,
                        color = accentColor,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0x1AFFFFFF), RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = meal.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFcbd5e1),
                            lineHeight = 22.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Text(
                        text = stringResource(R.string.features_health_nutrition_label_physiological_impact),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Row(modifier = Modifier.padding(top = 12.dp)) {
                        NutritionItem(label = "CALORIES", value = "${meal.nutrition.calories}", color = Color.White)
                        NutritionItem(label = "PROTEIN", value = "${meal.nutrition.protein}g", color = accentColor)
                        NutritionItem(label = "CARBS", value = "${meal.nutrition.carbs}g", color = Color.White)
                        NutritionItem(label = "FAT", value = "${meal.nutrition.fat}g", color = Color.White)
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Text(
                        text = stringResource(R.string.features_health_nutrition_label_metabolic_matrix),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }
            }
            
            items(meal.ingredients) { ingredient ->
                Row(
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 6.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(6.dp).background(accentColor, RoundedCornerShape(50)))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = ingredient.name, modifier = Modifier.weight(1f), color = Color(0xFFcbd5e1))
                    Text(text = ingredient.amount, color = Color(0xFF94a3b8), style = MaterialTheme.typography.bodySmall)
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = stringResource(R.string.features_health_nutrition_label_protocol_execution),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            items(meal.steps) { step ->
                Column(
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                        .background(Color(0x0DFFFFFF), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "STAGE ${step.order}",
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = step.instruction,
                        color = Color(0xFFcbd5e1),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = onStartCooking,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(64.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BioOptimizedColors.Cyan400,
                        contentColor = BioOptimizedColors.Slate950
                    ),
                    shape = RoundedCornerShape(32.dp)
                ) {
                    Icon(Icons.Default.Restaurant, null)
                    Spacer(Modifier.width(12.dp))
                    Text(stringResource(R.string.features_health_nutrition_action_start_cooking), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@Composable
private fun NutritionItem(label: String, value: String, color: Color) {
    Column(modifier = Modifier.padding(end = 24.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color(0xFF94a3b8))
        Text(text = value, style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.Bold)
    }
}
