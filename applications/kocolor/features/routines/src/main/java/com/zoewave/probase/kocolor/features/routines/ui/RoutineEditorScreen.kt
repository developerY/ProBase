package com.zoewave.probase.kocolor.features.routines.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zoewave.probase.kocolor.model.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RoutineEditorScreen(
    uiState: RoutinesUiState,
    onEvent: (RoutinesEvent) -> Unit,
    onBack: () -> Unit
) {
    val routine = uiState.activeEditRoutine ?: return
    val pagerState = rememberPagerState { routine.steps.size + 1 }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("CURATE RITUAL", style = MaterialTheme.typography.labelLarge, letterSpacing = 2.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.Close, null) }
                },
                actions = {
                    TextButton(onClick = { onEvent(RoutinesEvent.CloseEditDialog); onBack() }) {
                        Text("SAVE", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Editorial Progress
            LinearProgressIndicator(
                progress = { (pagerState.currentPage + 1).toFloat() / pagerState.pageCount },
                modifier = Modifier.fillMaxWidth().height(2.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                userScrollEnabled = false
            ) { pageIndex ->
                if (pageIndex < routine.steps.size) {
                    StepEditorPage(
                        step = routine.steps[pageIndex],
                        allProducts = uiState.allProducts,
                        onEvent = onEvent,
                        routineId = routine.id
                    )
                } else {
                    AddStepPage(
                        draftStep = uiState.draftStep,
                        onEvent = onEvent,
                        routineId = routine.id,
                        onStepAdded = {
                            scope.launch { pagerState.animateScrollToPage(routine.steps.size) }
                        }
                    )
                }
            }

            // High-End Navigation Controls
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 16.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
                        enabled = pagerState.currentPage > 0
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("PREVIOUS", style = MaterialTheme.typography.labelLarge)
                    }

                    if (pagerState.currentPage < pagerState.pageCount - 1) {
                        Button(
                            onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                        ) {
                            Text("NEXT STEP", style = MaterialTheme.typography.labelLarge)
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(18.dp))
                        }
                    } else {
                        Button(
                            onClick = { onEvent(RoutinesEvent.CloseEditDialog); onBack() },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                        ) {
                            Text("FINISH CURATION", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepEditorPage(
    step: RoutineStep,
    allProducts: List<CosmeticItem>,
    onEvent: (RoutinesEvent) -> Unit,
    routineId: Long
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(28.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        Column {
            Text(
                text = step.title,
                style = MaterialTheme.typography.displaySmall,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                lineHeight = 44.sp
            )
            Text(
                text = "Curate the performance for this ritual stage.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "VAULT SELECTION", 
                style = MaterialTheme.typography.labelSmall, 
                fontWeight = FontWeight.Black, 
                letterSpacing = 2.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(16.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(allProducts) { product ->
                    val isLinked = step.productIds.contains(product.id)
                    ProductSelectionCard(
                        product = product,
                        isSelected = isLinked,
                        onClick = { onEvent(RoutinesEvent.LinkProduct(routineId, step.id, product.id)) }
                    )
                }
            }
        }
        
        TextButton(
            onClick = { onEvent(RoutinesEvent.RemoveStep(routineId, step.id)) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.Default.DeleteOutline, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("REMOVE THIS STAGE", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun AddStepPage(
    draftStep: RoutineStep,
    onEvent: (RoutinesEvent) -> Unit,
    routineId: Long,
    onStepAdded: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
            shape = CircleShape,
            modifier = Modifier.size(80.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
            }
        }
        
        Spacer(Modifier.height(32.dp))
        
        Text(
            text = "Enhance Your Ritual.",
            style = MaterialTheme.typography.displaySmall,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Add a new stage to your guided beauty flow.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(Modifier.height(48.dp))
        
        OutlinedTextField(
            value = draftStep.title,
            onValueChange = { onEvent(RoutinesEvent.UpdateDraftStep(draftStep.copy(title = it))) },
            placeholder = { Text("e.g. Gua Sha Revitalization", style = MaterialTheme.typography.bodyLarge) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            textStyle = MaterialTheme.typography.headlineSmall
        )
        
        Spacer(Modifier.height(32.dp))
        
        Button(
            onClick = { 
                onEvent(RoutinesEvent.AddStep(routineId))
                onStepAdded()
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = draftStep.title.isNotBlank()
        ) {
            Icon(Icons.Default.Add, null)
            Spacer(Modifier.width(8.dp))
            Text("ADD RITUAL STAGE", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun ProductSelectionCard(
    product: CosmeticItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val alpha by animateFloatAsState(if (isSelected) 1f else 0.5f, label = "alpha")
    val borderAlpha by animateFloatAsState(if (isSelected) 1f else 0f, label = "border")

    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .alpha(alpha),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = borderAlpha), RoundedCornerShape(20.dp))
        ) {
            if (product.imageUrl != null) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            
            if (isSelected) {
                Box(
                    modifier = Modifier.align(Alignment.TopEnd).padding(6.dp).size(20.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(12.dp))
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = product.name,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            maxLines = 1,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
