package com.zoewave.probase.kocolor.features.routines.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.kocolor.features.routines.R
import com.zoewave.probase.kocolor.features.routines.ui.components.*
import com.zoewave.probase.kocolor.model.*

data class RoutineEditorUiState(
    val initialStepId: String? = null,
    val routinesUiState: RoutinesUiState,
    val onBack: (() -> Unit)? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineEditorScreen(
    uiState: RoutineEditorUiState,
    onEvent: (RoutinesEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val initialStepId = uiState.initialStepId
    val state = uiState.routinesUiState
    val onBack = uiState.onBack ?: {}
    
    val routine = state.activeEditRoutine ?: return
    var editingStepId by remember { mutableStateOf(initialStepId) }
    
    var newStepDraft by remember { 
        mutableStateOf(
            if (initialStepId == "new_step") {
                RoutineStep(
                    id = java.util.UUID.randomUUID().toString(),
                    title = "New Ritual Stage",
                    layeringOrder = routine.steps.size
                )
            } else null
        )
    }

    var selectionStage by remember { 
        mutableStateOf(if (initialStepId == "new_step") ProductSelectionStage.MainForm else ProductSelectionStage.HeroPage) 
    }

    var selectedMacro by remember { mutableStateOf<MacroCategory?>(null) }
    var selectedMicro by remember { mutableStateOf<MicroCategory?>(null) }

    val activeStep = newStepDraft ?: routine.steps.find { it.id == editingStepId }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = when (selectionStage) {
                            ProductSelectionStage.HeroPage -> if (editingStepId == null) "Curate Ritual" else "Ritual Knowledge"
                            ProductSelectionStage.MainForm -> if (newStepDraft != null) "New Stage" else "Edit Stage"
                            ProductSelectionStage.Macro -> "Select Category"
                            ProductSelectionStage.Micro -> "Select Type"
                            ProductSelectionStage.Item -> "Select Product"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = FontFamily.Serif
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { 
                        when (selectionStage) {
                            ProductSelectionStage.HeroPage -> { if (editingStepId != null) editingStepId = null else onBack() }
                            ProductSelectionStage.MainForm -> { if (newStepDraft != null) onBack() else selectionStage = ProductSelectionStage.HeroPage }
                            ProductSelectionStage.Macro -> selectionStage = ProductSelectionStage.MainForm
                            ProductSelectionStage.Micro -> selectionStage = ProductSelectionStage.Macro
                            ProductSelectionStage.Item -> selectionStage = ProductSelectionStage.Micro
                        }
                    }) { 
                        Icon(if (selectionStage == ProductSelectionStage.HeroPage && editingStepId == null) Icons.Default.Close else Icons.AutoMirrored.Filled.ArrowBack, null) 
                    }
                },
                actions = {
                    if (selectionStage == ProductSelectionStage.MainForm) {
                        TextButton(onClick = { 
                            if (newStepDraft != null) {
                                onEvent(RoutinesEvent.UpdateRoutine(routine.copy(steps = routine.steps + newStepDraft!!)))
                            }
                            onEvent(RoutinesEvent.CloseEditDialog); onBack() 
                        }) {
                            Text(stringResource(R.string.applications_kocolor_features_routines_save), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (editingStepId == null && newStepDraft == null) {
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(routine.steps) { step ->
                        StepSummaryRow(uiState = step, onEvent = { editingStepId = step.id; selectionStage = ProductSelectionStage.HeroPage })
                    }
                }
            } else if (activeStep != null) {
                when (selectionStage) {
                    ProductSelectionStage.HeroPage -> StepHeroPage(
                        uiState = Triple(activeStep, state.allProducts, routine.id), 
                        onEvent = onEvent,
                        onEditStage = { selectionStage = ProductSelectionStage.MainForm },
                        navTo = navTo
                    )
                    ProductSelectionStage.MainForm -> EditStepForm(uiState = activeStep to state.allProducts, onEvent = { event ->
                        when (event) {
                            "product" -> selectionStage = ProductSelectionStage.Macro
                            "remove" -> { if (newStepDraft != null) onBack() else { onEvent(RoutinesEvent.RemoveStep(routine.id, activeStep.id)); editingStepId = null } }
                        }
                    })
                    ProductSelectionStage.Macro -> MacroSelectionPage(onEvent = { macro -> selectedMacro = macro; selectionStage = ProductSelectionStage.Micro })
                    ProductSelectionStage.Micro -> MicroSelectionPage(macro = selectedMacro!!, onEvent = { micro -> selectedMicro = micro; selectionStage = ProductSelectionStage.Item })
                    ProductSelectionStage.Item -> ItemSelectionPage(uiState = Triple(state.allProducts.filter { it.microCategory == selectedMicro }, activeStep.productIds, { productId: Long ->
                        if (newStepDraft != null) {
                            val newIds = if (newStepDraft!!.productIds.contains(productId)) newStepDraft!!.productIds - productId else newStepDraft!!.productIds + productId
                            newStepDraft = newStepDraft!!.copy(productIds = newIds)
                        } else {
                            onEvent(RoutinesEvent.LinkProduct(routine.id, activeStep.id, productId))
                        }
                        selectionStage = ProductSelectionStage.MainForm
                    }))
                }
            }
        }
    }
}

enum class ProductSelectionStage { HeroPage, MainForm, Macro, Micro, Item }

@Preview(showBackground = true)
@Composable
private fun RoutineEditorScreenPreview() {
    MaterialTheme {
        RoutineEditorScreen(
            uiState = RoutineEditorUiState(
                routinesUiState = RoutinesUiState(
                    morningRoutine = BeautyRoutine(id = 1L, title = "Morning", time = RoutineTime.MORNING, steps = emptyList(), date = 0),
                    activeEditRoutineId = 1L
                )
            ),
            onEvent = {},
            navTo = {}
        )
    }
}
