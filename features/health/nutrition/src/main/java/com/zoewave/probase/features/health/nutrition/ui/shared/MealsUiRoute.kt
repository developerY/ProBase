package com.zoewave.probase.features.health.nutrition.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoewave.probase.features.health.nutrition.ui.shared.AddMealCameraScreen
import com.zoewave.probase.features.health.nutrition.ui.shared.BioMealCard
import com.zoewave.probase.features.health.nutrition.ui.shared.EditMealScreen
import com.zoewave.probase.features.health.nutrition.ui.shared.MealDetailScreen
import com.zoewave.probase.features.health.nutrition.ui.shared.MealPreparationScreen

@Composable
fun MealsUiRoute(
    onBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MealsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MealsUiRoute(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        onNavigateToHome = onNavigateToHome,
        modifier = modifier
    )
}

@Composable
internal fun MealsUiRoute(
    uiState: MealsUiState,
    onEvent: (MealsUiEvent) -> Unit,
    onBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = BioOptimizedColors.Slate950
    ) { padding ->
        MealsContent(
            uiState = uiState,
            onEvent = onEvent,
            onBack = onBack,
            onNavigateToHome = onNavigateToHome,
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
private fun MealsContent(
    uiState: MealsUiState,
    onEvent: (MealsUiEvent) -> Unit,
    onBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (uiState) {
        MealsUiState.Loading -> {
            Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BioOptimizedColors.Cyan400)
            }
        }
        is MealsUiState.Error -> {
            Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(uiState.message, color = BioOptimizedColors.Pink400)
            }
        }
        is MealsUiState.Success -> {
            if (uiState.cookingMeal != null) {
                MealPreparationScreen(
                    meal = uiState.cookingMeal,
                    currentStepIndex = uiState.currentPreparationStep,
                    onStepClick = { onEvent(MealsUiEvent.SetPreparationStep(it)) },
                    onNext = { onEvent(MealsUiEvent.SetPreparationStep(uiState.currentPreparationStep + 1)) },
                    onPrevious = { onEvent(MealsUiEvent.SetPreparationStep(uiState.currentPreparationStep - 1)) },
                    onFinish = { onNavigateToHome() },
                    onBack = { onEvent(MealsUiEvent.StartCooking(null)) }
                )
            } else if (uiState.editingMeal != null) {
                EditMealScreen(
                    meal = uiState.editingMeal,
                    onSave = { onEvent(MealsUiEvent.UpdateMeal(it)) },
                    onBack = { onEvent(MealsUiEvent.EditMeal(null)) }
                )
            } else if (uiState.selectedMeal != null) {
                MealDetailScreen(
                    meal = uiState.selectedMeal,
                    onEdit = { onEvent(MealsUiEvent.EditMeal(uiState.selectedMeal)) },
                    onDelete = { onEvent(MealsUiEvent.DeleteMeal(uiState.selectedMeal.id)) },
                    onStartCooking = { onEvent(MealsUiEvent.StartCooking(uiState.selectedMeal)) },
                    onBack = { onEvent(MealsUiEvent.SelectMeal(null)) }
                )
            } else if (uiState.isAddingMeal) {
                AddMealCameraScreen(
                    onCapture = { onEvent(MealsUiEvent.AddCapturedMeal(it)) },
                    onBack = { onEvent(MealsUiEvent.SetAddingMeal(false)) }
                )
            } else {
                MealsListScreen(
                    uiState = uiState,
                    onEvent = onEvent,
                    onBack = onBack,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MealsListScreen(
    uiState: MealsUiState.Success,
    onEvent: (MealsUiEvent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Metabolic Meals", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BioOptimizedColors.Slate950)
            )
        },
        floatingActionButton = {
            LargeFloatingActionButton(
                onClick = { onEvent(MealsUiEvent.SetAddingMeal(true)) },
                containerColor = BioOptimizedColors.Cyan400,
                contentColor = BioOptimizedColors.Slate950,
                shape = CircleShape
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Add Meal", modifier = Modifier.size(32.dp))
            }
        },
        containerColor = BioOptimizedColors.Slate950,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "BioOptimized Protocols",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "SYNC YOUR FEEDING WITH METABOLIC PEAKS",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF94a3b8),
                        letterSpacing = 1.sp
                    )
                }
            }

            items(uiState.meals) { meal ->
                BioMealCard(
                    meal = meal,
                    onClick = { onEvent(MealsUiEvent.SelectMeal(meal)) }
                )
            }
            
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}
