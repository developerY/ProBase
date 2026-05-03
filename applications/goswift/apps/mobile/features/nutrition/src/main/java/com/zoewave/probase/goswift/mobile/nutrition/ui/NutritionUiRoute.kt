package com.zoewave.probase.goswift.mobile.nutrition.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoewave.probase.goswift.mobile.nutrition.R
import com.zoewave.probase.goswift.mobile.nutrition.ui.components.CalorieBubbleContainer
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun NutritionUiRoute(
    modifier: Modifier = Modifier,
    viewModel: NutritionViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    NutritionUiRoute(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        modifier = modifier
    )
}

@Composable
internal fun NutritionUiRoute(
    uiState: NutritionUiState,
    onEvent: (NutritionUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    NutritionScreen(
        uiState = uiState,
        onEvent = onEvent,
        modifier = modifier
    )
}

@Composable
fun NutritionScreen(
    uiState: NutritionUiState,
    onEvent: (NutritionUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    var foodName by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (uiState) {
            NutritionUiState.Loading -> {
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is NutritionUiState.Success -> {
                CalorieBubbleContainer(
                    meals = uiState.recentMeals,
                    modifier = Modifier.height(250.dp)
                )

                DailyCalorieCard(uiState.dailyCalories)
                
                Spacer(Modifier.height(24.dp))
                
                Text(stringResource(R.string.applications_goswift_apps_mobile_features_nutrition_log_meal), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = foodName,
                    onValueChange = { foodName = it },
                    label = { Text(stringResource(R.string.applications_goswift_apps_mobile_features_nutrition_food_name)) },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = calories,
                    onValueChange = { calories = it },
                    label = { Text(stringResource(R.string.applications_goswift_apps_mobile_features_nutrition_calories_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(Modifier.height(16.dp))
                
                Button(
                    onClick = {
                        val calValue = calories.toDoubleOrNull()
                        if (foodName.isNotBlank() && calValue != null) {
                            onEvent(NutritionUiEvent.AddMeal(foodName, calValue))
                            foodName = ""
                            calories = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.applications_goswift_apps_mobile_features_nutrition_add_log))
                }
                
                Spacer(Modifier.height(24.dp))
                
                Text(
                    text = stringResource(R.string.applications_goswift_apps_mobile_features_nutrition_recent_meals), 
                    style = MaterialTheme.typography.titleMedium, 
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(Modifier.height(8.dp))
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(uiState.recentMeals) { log ->
                        MealLogItem(log)
                    }
                }
            }
        }
    }
}

@Composable
fun DailyCalorieCard(calories: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.applications_goswift_apps_mobile_features_nutrition_daily_calories), 
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = stringResource(R.string.applications_goswift_apps_mobile_features_nutrition_kcal_format, calories), 
                style = MaterialTheme.typography.displayMedium
            )
        }
    }
}

@Composable
fun MealLogItem(log: MealLog) {
    val time = Instant.ofEpochMilli(log.timestamp)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("HH:mm"))
        
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(log.name, style = MaterialTheme.typography.bodyLarge)
                Text(time, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
            }
            Text(
                text = stringResource(R.string.applications_goswift_apps_mobile_features_nutrition_kcal_format, log.calories), 
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
