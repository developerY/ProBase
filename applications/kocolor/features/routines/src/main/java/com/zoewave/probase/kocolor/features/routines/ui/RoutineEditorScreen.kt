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
    val pagerState = rememberPagerState { routine.steps.size + 1 } // +1 for the "Add Step" or "Summary" page
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(routine.title.uppercase(), style = MaterialTheme.typography.labelLarge, letterSpacing = 2.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.Close, null) }
                },
                actions = {
                    TextButton(onClick = { onEvent(RoutinesEvent.CloseEditDialog); onBack() }) {
                        Text("SAVE", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Progress Indicator
            LinearProgressIndicator(
                progress = { (pagerState.currentPage + 1).toFloat() / pagerState.pageCount },
                modifier = Modifier.fillMaxWidth().height(4.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                userScrollEnabled = false // Guided navigation
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

            // Navigation Controls
            Row(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
                    enabled = pagerState.currentPage > 0
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    Spacer(Modifier.width(8.dp))
                    Text("PREVIOUS")
                }

                if (pagerState.currentPage < pagerState.pageCount - 1) {
                    Button(
                        onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("NEXT")
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null)
                    }
                } else {
                    Button(
                        onClick = { onEvent(RoutinesEvent.CloseEditDialog); onBack() },
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("FINISH RITUAL")
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
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        Column {
            Text(
                text = step.title,
                style = MaterialTheme.typography.displaySmall,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Curate the products for this step.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text("SELECT PRODUCTS FROM YOUR VAULT", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, letterSpacing = 1.sp)

        LazyVerticalGrid(
            columns = GridCells.Adaptive(100.dp),
            modifier = Modifier.weight(1f),
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
        
        OutlinedButton(
            onClick = { onEvent(RoutinesEvent.RemoveStep(routineId, step.id)) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            border = borderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
        ) {
            Icon(Icons.Default.Delete, null)
            Spacer(Modifier.width(8.dp))
            Text("REMOVE THIS STEP")
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
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Enhance Your Ritual.",
            style = MaterialTheme.typography.displaySmall,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Add a new meaningful step to your routine.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(Modifier.height(48.dp))
        
        OutlinedTextField(
            value = draftStep.title,
            onValueChange = { onEvent(RoutinesEvent.UpdateDraftStep(draftStep.copy(title = it))) },
            placeholder = { Text("e.g. Gua Sha Massage") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            textStyle = MaterialTheme.typography.headlineSmall
        )
        
        Spacer(Modifier.height(32.dp))
        
        Button(
            onClick = { 
                onEvent(RoutinesEvent.AddStep(routineId))
                onStepAdded()
            },
            modifier = Modifier.fillMaxWidth().height(64.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = draftStep.title.isNotBlank()
        ) {
            Icon(Icons.Default.Add, null)
            Spacer(Modifier.width(8.dp))
            Text("ADD STEP TO RITUAL")
        }
    }
}

@Composable
private fun ProductSelectionCard(
    product: CosmeticItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val alpha by animateFloatAsState(if (isSelected) 1f else 0.4f, label = "alpha")
    val borderAlpha by animateFloatAsState(if (isSelected) 1f else 0f, label = "border")

    Card(
        modifier = Modifier
            .aspectRatio(0.8f)
            .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = borderAlpha), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (product.imageUrl != null) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().alpha(alpha),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(modifier = Modifier.fillMaxSize().background(parseColor(product.colorHex ?: "#CCCCCC")).alpha(alpha))
            }
            
            Box(
                modifier = Modifier.fillMaxSize().padding(12.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp)
                )
            }
            
            if (isSelected) {
                Box(
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).size(24.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

private fun borderStroke(width: androidx.compose.ui.unit.Dp, color: Color) = androidx.compose.foundation.BorderStroke(width, color)

private fun parseColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        Color.Gray
    }
}
