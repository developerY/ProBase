package com.zoewave.probase.features.health.nutrition.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoewave.probase.features.health.nutrition.ui.components.NutritionRitualHeader
import com.zoewave.probase.features.health.nutrition.ui.components.NutritionRitualStep
import com.zoewave.probase.kocolor.model.BeautyRoutine
import com.zoewave.probase.kocolor.model.RoutineStep

@Composable
fun NutritionUiRoute(
    onBack: () -> Unit,
    onNavigateToKnowledgeHub: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NutritionViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    NutritionUiRoute(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        onNavigateToKnowledgeHub = onNavigateToKnowledgeHub,
        modifier = modifier
    )
}

@Composable
internal fun NutritionUiRoute(
    uiState: NutritionUiState,
    onEvent: (NutritionUiEvent) -> Unit,
    onBack: () -> Unit,
    onNavigateToKnowledgeHub: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    NutritionScreen(
        uiState = uiState,
        onEvent = onEvent,
        onBack = onBack,
        onNavigateToKnowledgeHub = onNavigateToKnowledgeHub,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionScreen(
    uiState: NutritionUiState,
    onEvent: (NutritionUiEvent) -> Unit,
    onBack: () -> Unit,
    onNavigateToKnowledgeHub: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = "Meals Ritual", 
                        fontFamily = FontFamily.Serif, 
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent,
        modifier = modifier
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFE1F5FE), Color.White)
                    )
                )
                .padding(padding)
        ) {
            when (uiState) {
                NutritionUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is NutritionUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(uiState.message, color = MaterialTheme.colorScheme.error)
                    }
                }
                is NutritionUiState.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        val routine: BeautyRoutine = uiState.routine
                        
                        NutritionRitualHeader(
                            completedCount = routine.steps.count { it.isCompleted },
                            totalCount = routine.steps.size,
                            nextWindow = uiState.nextMetabolicWindow
                        )

                        routine.steps.forEach { step: RoutineStep ->
                            NutritionRitualStep(
                                step = step,
                                onToggle = { onEvent(NutritionUiEvent.ToggleStage(step.id)) },
                                onKnowledgeHub = { onNavigateToKnowledgeHub(step.id) }
                            )
                        }

                        Spacer(Modifier.height(48.dp))
                    }
                }
            }
        }
    }
}
