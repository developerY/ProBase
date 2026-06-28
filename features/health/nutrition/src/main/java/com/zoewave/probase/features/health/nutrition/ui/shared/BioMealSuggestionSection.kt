package com.zoewave.probase.features.health.nutrition.ui.shared

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoewave.probase.features.health.nutrition.data.Meal
import com.zoewave.probase.features.health.nutrition.data.MetabolicPhase
import com.zoewave.probase.features.health.nutrition.data.NutritionInfo
import com.zoewave.probase.features.health.nutrition.ui.shared.BioOptimizedColors
import com.zoewave.probase.features.health.nutrition.ui.shared.MealsUiState
import com.zoewave.probase.features.health.nutrition.ui.shared.MealsViewModel

@Preview(showBackground = true, backgroundColor = 0xFF020617)
@Composable
private fun BioMealSuggestionSectionPreview() {
    MaterialTheme {
        BioMealSuggestionSection(
            uiState = MealsUiState.Success(
                meals = listOf(
                    Meal(
                        id = "1",
                        name = "Golden Turmeric Elixir",
                        description = "Morning tonic",
                        scientificFocus = "Anti-inflammatory",
                        phase = MetabolicPhase.Morning,
                        nutrition = NutritionInfo(120, 2f, 15f, 5f),
                        ingredients = emptyList(),
                        steps = emptyList()
                    ),
                    Meal(
                        id = "2",
                        name = "Quinoa Power Bowl",
                        description = "Lunch bowl",
                        scientificFocus = "Microbiome support",
                        phase = MetabolicPhase.MidDay,
                        nutrition = NutritionInfo(450, 15f, 60f, 12f),
                        ingredients = emptyList(),
                        steps = emptyList()
                    )
                )
            ),
            currentMealId = "1",
            onMealSelected = {}
        )
    }
}

@Composable
fun BioMealSuggestionSection(
    currentMealId: String?,
    onMealSelected: (Meal) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MealsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    BioMealSuggestionSection(
        uiState = uiState,
        currentMealId = currentMealId,
        onMealSelected = onMealSelected,
        modifier = modifier
    )
}

@Composable
fun BioMealSuggestionSection(
    uiState: MealsUiState,
    currentMealId: String?,
    onMealSelected: (Meal) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "MEAL SUGGESTIONS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = Color.Gray,
                letterSpacing = 1.sp
            )
            
            if (uiState is MealsUiState.Success) {
                Text(
                    text = "${uiState.meals.size} Options",
                    style = MaterialTheme.typography.labelSmall,
                    color = BioOptimizedColors.Cyan400
                )
            }
        }

        when (uiState) {
            MealsUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            }
            is MealsUiState.Success -> {
                val meals = uiState.meals
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(meals) { meal ->
                        val isSelected = meal.id == currentMealId
                        
                        Box(contentAlignment = Alignment.TopEnd) {
                            BioMealCard(
                                meal = meal,
                                onClick = { 
                                    onMealSelected(meal)
                                },
                                modifier = Modifier.width(320.dp)
                            )
                            
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = BioOptimizedColors.Cyan400,
                                    modifier = Modifier.padding(12.dp).size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
            else -> {
                Text("No suggestions available.", color = Color.Gray)
            }
        }
    }
}
