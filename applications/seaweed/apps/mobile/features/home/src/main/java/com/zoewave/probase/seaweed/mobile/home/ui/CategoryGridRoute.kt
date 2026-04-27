package com.zoewave.probase.seaweed.mobile.home.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Merge
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoewave.probase.seaweed.mobile.core.ui.components.CategoryQuickJumpCard
import com.zoewave.probase.seaweed.mobile.home.R
import com.zoewave.probase.seaweed.model.CategoryOverview
import com.zoewave.probase.seaweed.model.navigation.SeaweedDestination
import com.zoewave.probase.core.ui.R as CoreUiR

@Composable
fun CategoryGridRoute(
    navTo: (SeaweedDestination) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CategoryGridScreen(
        uiState = uiState,
        onEvent = { event ->
            if (event is HomeUiEvent.OnBackClicked) {
                onBack()
            } else {
                viewModel.onEvent(event)
            }
        },
        navTo = navTo,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryGridScreen(
    uiState: HomeUiState,
    onEvent: (HomeUiEvent) -> Unit,
    navTo: (SeaweedDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    var categoryToDelete by remember { mutableStateOf<String?>(null) }
    var showMenu by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showCombineSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.applications_seaweed_apps_mobile_features_home_all_categories)) },
                navigationIcon = {
                    IconButton(onClick = { onEvent(HomeUiEvent.OnBackClicked) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = stringResource(CoreUiR.string.cd_navigate_back)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            CategoryGridFab(
                showMenu = showMenu,
                onToggleMenu = { showMenu = it },
                onAddClick = { showAddDialog = true },
                onCombineClick = { showCombineSheet = true }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        when (uiState) {
            HomeUiState.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is HomeUiState.Success -> {
                CategoryGridContent(
                    categories = uiState.profile.categoryOverviews,
                    onCategoryClick = { navTo(SeaweedDestination.Transactions(it.name)) },
                    onCategoryDelete = { categoryToDelete = it.name },
                    modifier = Modifier.padding(padding)
                )

                if (showAddDialog) {
                    AddCategoryDialog(
                        onDismiss = { showAddDialog = false },
                        onConfirm = { 
                            onEvent(HomeUiEvent.AddCategory(it))
                            showAddDialog = false
                        }
                    )
                }

                if (showCombineSheet) {
                    CombineCategoriesBottomSheet(
                        categories = uiState.profile.categoryOverviews.map { it.name },
                        onDismiss = { showCombineSheet = false },
                        onCombine = { from, to ->
                            onEvent(HomeUiEvent.CombineCategories(from, to))
                            showCombineSheet = false
                        }
                    )
                }
            }
        }
    }

    if (categoryToDelete != null) {
        DeleteCategoryConfirmDialog(
            categoryName = categoryToDelete!!,
            onDismiss = { categoryToDelete = null },
            onConfirm = {
                onEvent(HomeUiEvent.DeleteCategory(categoryToDelete!!))
                categoryToDelete = null
            }
        )
    }
}

@Composable
private fun CategoryGridContent(
    categories: List<CategoryOverview>,
    onCategoryClick: (CategoryOverview) -> Unit,
    onCategoryDelete: (CategoryOverview) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 80.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(categories) { category ->
            CategoryQuickJumpCard(
                category = category,
                onClick = { onCategoryClick(category) },
                onDelete = { onCategoryDelete(category) }
            )
        }
    }
}

@Composable
private fun CategoryGridFab(
    showMenu: Boolean,
    onToggleMenu: (Boolean) -> Unit,
    onAddClick: () -> Unit,
    onCombineClick: () -> Unit
) {
    Box {
        FloatingActionButton(onClick = { onToggleMenu(true) }) {
            Icon(
                Icons.Default.Add, 
                contentDescription = stringResource(R.string.applications_seaweed_apps_mobile_features_home_actions_cd)
            )
        }
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { onToggleMenu(false) }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(CoreUiR.string.core_ui_add_new_category)) },
                onClick = {
                    onAddClick()
                    onToggleMenu(false)
                },
                leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.applications_seaweed_apps_mobile_features_home_combine_categories)) },
                onClick = {
                    onCombineClick()
                    onToggleMenu(false)
                },
                leadingIcon = { Icon(Icons.Default.Merge, contentDescription = null) }
            )
        }
    }
}

@Composable
private fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var newCategoryName by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(CoreUiR.string.core_ui_add_new_category)) },
        text = {
            OutlinedTextField(
                value = newCategoryName,
                onValueChange = { newCategoryName = it },
                label = { Text(stringResource(CoreUiR.string.core_ui_category_name)) },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (newCategoryName.isNotBlank()) {
                        onConfirm(newCategoryName)
                    }
                }
            ) { Text(stringResource(CoreUiR.string.core_ui_action_add)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(CoreUiR.string.action_cancel)) }
        }
    )
}

@Composable
private fun DeleteCategoryConfirmDialog(
    categoryName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.applications_seaweed_apps_mobile_features_home_delete_category)) },
        text = { 
            Text(
                stringResource(
                    R.string.applications_seaweed_apps_mobile_features_home_delete_category_confirm, 
                    categoryName
                )
            ) 
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(CoreUiR.string.action_delete), 
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(CoreUiR.string.action_cancel))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CombineCategoriesBottomSheet(
    categories: List<String>,
    onDismiss: () -> Unit,
    onCombine: (String, String) -> Unit
) {
    var fromCategory by remember { mutableStateOf(categories.firstOrNull() ?: "") }
    var toCategory by remember { mutableStateOf(categories.getOrNull(1) ?: categories.firstOrNull() ?: "") }
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
                text = stringResource(R.string.applications_seaweed_apps_mobile_features_home_combine_categories), 
                style = MaterialTheme.typography.titleLarge, 
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.applications_seaweed_apps_mobile_features_home_combine_desc), 
                style = MaterialTheme.typography.bodyMedium
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CategoryPicker(
                    label = stringResource(R.string.applications_seaweed_apps_mobile_features_home_label_from),
                    selected = fromCategory,
                    options = categories,
                    onSelected = { fromCategory = it },
                    modifier = Modifier.weight(1f)
                )
                CategoryPicker(
                    label = stringResource(R.string.applications_seaweed_apps_mobile_features_home_label_to),
                    selected = toCategory,
                    options = categories.filter { it != fromCategory },
                    onSelected = { toCategory = it },
                    modifier = Modifier.weight(1f)
                )
            }

            Button(
                onClick = { onCombine(fromCategory, toCategory) },
                modifier = Modifier.fillMaxWidth(),
                enabled = fromCategory.isNotBlank() && toCategory.isNotBlank() && fromCategory != toCategory
            ) {
                Text(stringResource(CoreUiR.string.core_ui_action_combine))
            }
        }
    }
}

@Composable
private fun CategoryPicker(
    label: String,
    selected: String,
    options: List<String>,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CategoryGridScreenSuccessPreview() {
    MaterialTheme {
        CategoryGridScreen(
            uiState = HomeUiState.Success(
                profile = com.zoewave.probase.seaweed.model.FinancialProfile(
                    monthlyIncomeCents = 500000L,
                    totalFixedCostsCents = 150000L,
                    realStartingBalanceCents = 350000L,
                    monthlyVariableSpendingCents = 100000L,
                    flexibleMoneyRemainingCents = 250000L,
                    totalBudgetedAmountCents = 200000L,
                    unallocatedMoneyCents = 150000L,
                    categoryOverviews = listOf(
                        CategoryOverview("food_id", "Food", 4200L, 1, 10000L, 5800L, 0.42f),
                        CategoryOverview("coffee_id", "Coffee", 1500L, 1, 5000L, 3500L, 0.3f)
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
private fun CategoryGridScreenLoadingPreview() {
    MaterialTheme {
        CategoryGridScreen(
            uiState = HomeUiState.Loading,
            onEvent = {},
            navTo = {}
        )
    }
}
