package com.zoewave.probase.seaweed.mobile.budget.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoewave.probase.seaweed.mobile.core.ui.components.CategoryBudgetProgressBar
import com.zoewave.probase.seaweed.mobile.core.ui.components.UnallocatedMoneyCard
import com.zoewave.probase.seaweed.model.CategoryOverview
import com.zoewave.probase.seaweed.model.FinancialProfile
import com.zoewave.probase.seaweed.model.navigation.SeaweedDestination
import java.util.Locale

@Composable
fun BudgetUiRoute(
    modifier: Modifier = Modifier,
    viewModel: BudgetViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BudgetScreen(
        uiState = uiState,
        onEvent = { event ->
            if (event is BudgetUiEvent.OnBackClicked) {
                onBack()
            } else {
                viewModel.onEvent(event)
            }
        },
        navTo = {},
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    uiState: BudgetUiState,
    onEvent: (BudgetUiEvent) -> Unit,
    navTo: (SeaweedDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Budget Management") },
                navigationIcon = {
                    IconButton(onClick = { onEvent(BudgetUiEvent.OnBackClicked) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        when (uiState) {
            BudgetUiState.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is BudgetUiState.Success -> {
                val profile = uiState.profile
                var editingCategory by remember { mutableStateOf<CategoryOverview?>(null) }

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        BudgetSummaryCard(profile = profile)
                    }

                    item {
                        UnallocatedMoneyCard(unallocatedAmount = profile.unallocatedMoney)
                    }

                    item {
                        Text(
                            text = "Category Budgets",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    items(profile.categoryOverviews) { category ->
                        BudgetItem(
                            category = category,
                            onEdit = { editingCategory = it },
                            onDelete = { onEvent(BudgetUiEvent.DeleteBudget(category.name)) }
                        )
                    }
                }

                if (editingCategory != null) {
                    BudgetEditBottomSheet(
                        category = editingCategory!!,
                        onDismiss = { editingCategory = null },
                        onSave = { limit ->
                            onEvent(BudgetUiEvent.UpdateBudget(editingCategory!!.name, limit))
                            editingCategory = null
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun BudgetSummaryCard(profile: FinancialProfile) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Total Monthly Budget", style = MaterialTheme.typography.labelSmall)
            Text(
                text = "$${String.format(Locale.getDefault(), "%.2f", profile.totalBudgetedAmount)}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { (profile.totalBudgetedAmount / profile.realStartingBalance).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f)
            )
            Text(
                text = "$${String.format(Locale.getDefault(), "%.0f", profile.realStartingBalance)} available after fixed costs",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun BudgetItem(
    category: CategoryOverview,
    onEdit: (CategoryOverview) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                CategoryBudgetProgressBar(
                    category = category,
                    modifier = Modifier.weight(1f)
                )
                Row {
                    IconButton(onClick = { onEdit(category) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(20.dp))
                    }
                    if (category.limitAmount != null) {
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BudgetEditBottomSheet(
    category: CategoryOverview,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    var limitInput by remember { mutableStateOf(category.limitAmount?.toString() ?: "") }
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Set Budget for ${category.name}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            OutlinedTextField(
                value = limitInput,
                onValueChange = { limitInput = it },
                label = { Text("Monthly Limit") },
                modifier = Modifier.fillMaxWidth(),
                prefix = { Text("$") }
            )

            Button(
                onClick = {
                    limitInput.toDoubleOrNull()?.let { onSave(it) }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = limitInput.toDoubleOrNull() != null
            ) {
                Text("Save Budget")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BudgetScreenPreview() {
    MaterialTheme {
        BudgetScreen(
            uiState = BudgetUiState.Success(
                profile = FinancialProfile(
                    monthlyIncome = 5000.0,
                    totalFixedCosts = 1500.0,
                    realStartingBalance = 3500.0,
                    monthlyVariableSpending = 1200.0,
                    flexibleMoneyRemaining = 2300.0,
                    totalBudgetedAmount = 2000.0,
                    unallocatedMoney = 1500.0,
                    categoryOverviews = listOf(
                        CategoryOverview("Food", 400.0, 15, 500.0, 100.0, 0.8f),
                        CategoryOverview("Coffee", 150.0, 20, 100.0, -50.0, 1.5f)
                    ),
                    monthProgress = 0.5f
                )
            ),
            onEvent = {},
            navTo = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BudgetScreenLoadingPreview() {
    MaterialTheme {
        BudgetScreen(
            uiState = BudgetUiState.Loading,
            onEvent = {},
            navTo = {}
        )
    }
}
