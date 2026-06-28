package com.zoewave.probase.features.health.nutrition.ui.shared

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.features.health.nutrition.R
import com.zoewave.probase.features.health.nutrition.data.Meal
import com.zoewave.probase.features.health.nutrition.data.MetabolicPhase
import com.zoewave.probase.features.health.nutrition.data.NutritionInfo
import com.zoewave.probase.features.health.nutrition.ui.shared.BioOptimizedColors

@Preview(showBackground = true)
@Composable
private fun EditMealScreenPreview() {
    MaterialTheme {
        EditMealScreen(
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
                ingredients = emptyList(),
                steps = emptyList()
            ),
            onSave = {},
            onBack = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMealScreen(
    meal: Meal,
    onSave: (Meal) -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf(meal.name) }
    var description by remember { mutableStateOf(meal.description) }
    var scientificFocus by remember { mutableStateOf(meal.scientificFocus) }
    var phase by remember { mutableStateOf(meal.phase) }
    
    var calories by remember { mutableStateOf(meal.nutrition.calories.toString()) }
    var protein by remember { mutableStateOf(meal.nutrition.protein.toString()) }
    var carbs by remember { mutableStateOf(meal.nutrition.carbs.toString()) }
    var fat by remember { mutableStateOf(meal.nutrition.fat.toString()) }

    val accentColor = when (phase) {
        MetabolicPhase.Morning -> BioOptimizedColors.Lime400
        MetabolicPhase.MidDay -> BioOptimizedColors.Cyan400
        MetabolicPhase.Evening -> BioOptimizedColors.Pink400
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.features_health_nutrition_action_edit_protocol), color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.features_health_nutrition_action_cancel), tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val updatedMeal = meal.copy(
                            name = name,
                            description = description,
                            scientificFocus = scientificFocus,
                            phase = phase,
                            nutrition = NutritionInfo(
                                calories = calories.toIntOrNull() ?: 0,
                                protein = protein.toFloatOrNull() ?: 0f,
                                carbs = carbs.toFloatOrNull() ?: 0f,
                                fat = fat.toFloatOrNull() ?: 0f
                            )
                        )
                        onSave(updatedMeal)
                    }) {
                        Icon(Icons.Default.Check, contentDescription = stringResource(R.string.features_health_nutrition_action_save), tint = accentColor)
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
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Basic Info
            BioTextField(label = stringResource(R.string.features_health_nutrition_label_protocol_name), value = name, onValueChange = { name = it })
            BioTextField(label = stringResource(R.string.features_health_nutrition_label_scientific_focus), value = scientificFocus, onValueChange = { scientificFocus = it }, color = accentColor)
            BioTextField(label = stringResource(R.string.features_health_nutrition_label_bio_rationale), value = description, onValueChange = { description = it }, singleLine = false)

            // Metabolic Phase Selection
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.features_health_nutrition_label_metabolic_phase), style = MaterialTheme.typography.labelSmall, color = Color(0xFF94a3b8))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PhaseChip(
                        phase = MetabolicPhase.Morning,
                        selected = phase == MetabolicPhase.Morning,
                        color = BioOptimizedColors.Lime400,
                        onClick = { phase = MetabolicPhase.Morning },
                        modifier = Modifier.weight(1f)
                    )
                    PhaseChip(
                        phase = MetabolicPhase.MidDay,
                        selected = phase == MetabolicPhase.MidDay,
                        color = BioOptimizedColors.Cyan400,
                        onClick = { phase = MetabolicPhase.MidDay },
                        modifier = Modifier.weight(1f)
                    )
                    PhaseChip(
                        phase = MetabolicPhase.Evening,
                        selected = phase == MetabolicPhase.Evening,
                        color = BioOptimizedColors.Pink400,
                        onClick = { phase = MetabolicPhase.Evening },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Nutrition Matrix
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.features_health_nutrition_label_nutritional_matrix), style = MaterialTheme.typography.labelSmall, color = Color(0xFF94a3b8))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    BioMetricField(label = "CAL", value = calories, onValueChange = { calories = it }, modifier = Modifier.weight(1f))
                    BioMetricField(label = "PRO (g)", value = protein, onValueChange = { protein = it }, modifier = Modifier.weight(1f), color = accentColor)
                    BioMetricField(label = "CHO (g)", value = carbs, onValueChange = { carbs = it }, modifier = Modifier.weight(1f))
                    BioMetricField(label = "FAT (g)", value = fat, onValueChange = { fat = it }, modifier = Modifier.weight(1f))
                }
            }
            
            Spacer(Modifier.height(48.dp))
        }
    }
}

@Composable
private fun BioTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    color: Color = Color.White,
    singleLine: Boolean = true
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color(0xFF94a3b8))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = color),
            singleLine = singleLine,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = color.copy(alpha = 0.5f),
                unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                cursorColor = color
            ),
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
private fun BioMetricField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    color: Color = Color.White
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color(0xFF94a3b8), modifier = Modifier.fillMaxWidth())
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = color, fontWeight = FontWeight.Bold),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = color.copy(alpha = 0.5f),
                unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                cursorColor = color
            ),
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
private fun PhaseChip(
    phase: MetabolicPhase,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) color.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            if (selected) color else Color.White.copy(alpha = 0.1f)
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = phase.name,
                style = MaterialTheme.typography.bodySmall,
                color = if (selected) color else Color(0xFF94a3b8),
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
