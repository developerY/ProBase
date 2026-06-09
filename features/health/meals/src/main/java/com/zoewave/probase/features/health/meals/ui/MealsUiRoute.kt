package com.zoewave.probase.features.health.meals.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.zoewave.probase.features.health.meals.data.MetabolicPhase
import com.zoewave.probase.features.health.meals.ui.components.AddMealCameraScreen
import com.zoewave.probase.features.health.meals.ui.components.MealCard
import com.zoewave.probase.features.health.meals.ui.components.MealDetailScreen

@Composable
fun MealsUiRoute(
    viewModel: MealsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    when (val s = state) {
        is MealsUiState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BioOptimizedColors.Slate950), 
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = BioOptimizedColors.Cyan400)
            }
        }
        is MealsUiState.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BioOptimizedColors.Slate950),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Error: ${s.message}", color = BioOptimizedColors.Pink400)
            }
        }
        is MealsUiState.Success -> {
            if (s.isAddingMeal) {
                AddMealCameraScreen(
                    onCapture = { uri -> viewModel.addCapturedMeal(uri) },
                    onBack = { viewModel.setAddingMeal(false) }
                )
            } else if (s.selectedMeal != null) {
                MealDetailScreen(
                    meal = s.selectedMeal,
                    onBack = { viewModel.selectMeal(null) }
                )
            } else {
                MealsListScreen(
                    meals = s.meals,
                    onMealClick = { viewModel.selectMeal(it) },
                    onAddClick = { viewModel.setAddingMeal(true) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MealsListScreen(
    meals: List<com.zoewave.probase.features.health.meals.data.Meal>,
    onMealClick: (com.zoewave.probase.features.health.meals.data.Meal) -> Unit,
    onAddClick: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "METABOLIC PROTOCOLS", 
                        color = Color.White, 
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        fontSize = 18.sp
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BioOptimizedColors.Slate950
                )
            )
        },
        containerColor = BioOptimizedColors.Slate950,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = BioOptimizedColors.Cyan400,
                contentColor = BioOptimizedColors.Slate950
            ) {
                Icon(Icons.Default.Add, contentDescription = "Capture Protocol")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            val groupedMeals = meals.groupBy { it.phase }
            
            MetabolicPhase.entries.forEach { phase ->
                val phaseMeals = groupedMeals[phase] ?: emptyList()
                if (phaseMeals.isNotEmpty()) {
                    item {
                        val accentColor = when (phase) {
                            MetabolicPhase.Morning -> BioOptimizedColors.Lime400
                            MetabolicPhase.MidDay -> BioOptimizedColors.Cyan400
                            MetabolicPhase.Evening -> BioOptimizedColors.Pink400
                        }
                        
                        Column {
                            Text(
                                text = phase.name.uppercase(),
                                style = MaterialTheme.typography.labelMedium,
                                color = accentColor,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(modifier = Modifier.height(2.dp).background(accentColor).fillMaxSize(0.1f))
                        }
                    }
                    items(phaseMeals) { meal ->
                        MealCard(meal = meal, onClick = { onMealClick(meal) })
                    }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}


