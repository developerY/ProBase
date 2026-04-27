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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoewave.probase.core.util.CurrencyUtils
import com.zoewave.probase.seaweed.mobile.budget.R
import com.zoewave.probase.seaweed.mobile.core.ui.components.CategoryBudgetProgressBar
import com.zoewave.probase.seaweed.mobile.core.ui.components.UnallocatedMoneyCard
import com.zoewave.probase.seaweed.model.CategoryOverview
import com.zoewave.probase.seaweed.model.FinancialProfile
import com.zoewave.probase.seaweed.model.navigation.SeaweedDestination
import com.zoewave.probase.core.ui.R as CoreUiR

@Composable
fun BudgetUiRoute(
    onBack: () -> Unit,
    navTo: (SeaweedDestination) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BudgetViewModel = hiltViewModel(),
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
        navTo = navTo,
        modifier = modifier
    )
}

@Composable
internal fun BudgetUiRoute(
    uiState: BudgetUiState,
    onEvent: (BudgetUiEvent) -> Unit,
    navTo: (SeaweedDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    BudgetScreen(
        uiState = uiState,
        onEvent = onEvent,
        navTo = navTo,
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
                title = { Text(stringResource(R.string.applications_seaweed_apps_mobile_features_budget_title)) },
                navigationIcon = {
                    IconButton(onClick = { onEvent(BudgetUiEvent.OnBackClicked) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = stringResource(CoreUiR.string.cd_navigate_back)
                        )
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
                BudgetContent(
                    profile = uiState.profile,
                    onEvent = onEvent,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
private fun BudgetContent(
    profile: FinancialProfile,
    onEvent: (BudgetUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    var editingCategory by remember { mutableStateOf<CategoryOverview?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            BudgetSummaryCard(profile = profile)
        }

        item {
            UnallocatedMoneyCard(unallocatedAmountCents = profile.unallocatedMoneyCents)
        }

        item {
            Text(
                text = stringResource(R.string.applications_seaweed_apps_mobile_features_budget_category_budgets),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(profile.categoryOverviews) { category ->
            BudgetItem(
                category = category,
                onEdit = { editingCategory = it },
                onDelete = { onEvent(BudgetUiEvent.DeleteBudget(category.id)) }
            )
        }
    }

    if (editingCategory != null) {
        BudgetEditBottomSheet(
            category = editingCategory!!,
            onDismiss = { editingCategory = null },
            onSave = { limit ->
                onEvent(BudgetUiEvent.UpdateBudget(editingCategory!!.id, limit))
                editingCategory = null
            }
        )
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
            Text(
                text = stringResource(R.string.applications_seaweed_apps_mobile_features_budget_total_monthly), 
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = stringResource(R.string.applications_seaweed_apps_mobile_features_budget_currency_format, CurrencyUtils.formatCents(profile.totalBudgetedAmountCents)),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { (profile.totalBudgetedAmountCents.toFloat() / profile.realStartingBalanceCents).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f)
            )
            Text(
                text = stringResource(
                    R.string.applications_seaweed_apps_mobile_features_budget_available_after_fixed, 
                    "$${CurrencyUtils.formatCents(profile.realStartingBalanceCents)}"
                ),
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
                        Icon(
                            Icons.Default.Edit, 
                            contentDescription = stringResource(CoreUiR.string.action_edit), 
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    if (category.limitAmountCents != null) {
                        IconButton(onClick = onDelete) {
                            Icon(
                                Icons.Default.Delete, 
                                contentDescription = stringResource(CoreUiR.string.action_delete), 
                                modifier = Modifier.size(20.dp)
                            )
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
    var limitInput by remember { mutableStateOf(category.limitAmountCents?.let { it.toDouble() / 100.0 }?.toString() ?: "") }
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
                text = stringResource(R.string.applications_seaweed_apps_mobile_features_budget_set_for, category.name),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            OutlinedTextField(
                value = limitInput,
                onValueChange = { limitInput = it },
                label = { Text(stringResource(R.string.applications_seaweed_apps_mobile_features_budget_monthly_limit)) },
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
                Text(stringResource(R.string.applications_seaweed_apps_mobile_features_budget_save))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BudgetScreenSuccessPreview() {
    MaterialTheme {
        BudgetScreen(
            uiState = BudgetUiState.Success(
                profile = FinancialProfile(
                    monthlyIncomeCents = 500000L,
                    totalFixedCostsCents = 150000L,
                    realStartingBalanceCents = 350000L,
                    monthlyVariableSpendingCents = 120000L,
                    flexibleMoneyRemainingCents = 230000L,
                    totalBudgetedAmountCents = 200000L,
                    unallocatedMoneyCents = 150000L,
                    categoryOverviews = listOf(
                        CategoryOverview("food_id", "Food", 40000L, 15, 50000L, 10000L, 0.8f),
                        CategoryOverview("coffee_id", "Coffee", 15000L, 20, 10000L, -5000L, 1.5f)
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
