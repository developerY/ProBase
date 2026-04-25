package com.zoewave.probase.seaweed.mobile.home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Merge
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    navTo: (SeaweedDestination) -> Unit,
    onBack: () -> Unit,
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
            Box {
                FloatingActionButton(onClick = { showMenu = true }) {
                    Icon(
                        Icons.Default.Add, 
                        contentDescription = stringResource(R.string.applications_seaweed_apps_mobile_features_home_actions_cd)
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(CoreUiR.string.core_ui_add_new_category)) },
                        onClick = {
                            showAddDialog = true
                            showMenu = false
                        },
                        leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.applications_seaweed_apps_mobile_features_home_combine_categories)) },
                        onClick = {
                            showCombineSheet = true
                            showMenu = false
                        },
                        leadingIcon = { Icon(Icons.Default.Merge, contentDescription = null) }
                    )
                }
            }
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
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 80.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.categoriesSummary) { category ->
                        CategoryQuickJumpCard(
                            category = category,
                            onClick = { navTo(SeaweedDestination.Transactions(category.name)) },
                            onDelete = { categoryToDelete = category.name }
                        )
                    }
                }

                if (showAddDialog) {
                    var newCategoryName by remember { mutableStateOf("") }
                    AlertDialog(
                        onDismissRequest = { showAddDialog = false },
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
                                        onEvent(HomeUiEvent.AddCategory(newCategoryName))
                                        showAddDialog = false
                                    }
                                }
                            ) { Text(stringResource(CoreUiR.string.core_ui_action_add)) }
                        },
                        dismissButton = {
                            TextButton(onClick = { showAddDialog = false }) { Text(stringResource(CoreUiR.string.action_cancel)) }
                        }
                    )
                }

                if (showCombineSheet) {
                    CombineCategoriesBottomSheet(
                        categories = uiState.categoriesSummary.map { it.name },
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
        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            title = { Text(stringResource(R.string.applications_seaweed_apps_mobile_features_home_delete_category)) },
            text = { 
                Text(
                    stringResource(
                        R.string.applications_seaweed_apps_mobile_features_home_delete_category_confirm, 
                        categoryToDelete!!
                    )
                ) 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onEvent(HomeUiEvent.DeleteCategory(categoryToDelete!!))
                        categoryToDelete = null
                    }
                ) {
                    Text(
                        text = stringResource(CoreUiR.string.action_delete), 
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { categoryToDelete = null }) {
                    Text(stringResource(CoreUiR.string.action_cancel))
                }
            }
        )
    }
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
private fun CategoryGridScreenPreview() {
    MaterialTheme {
        CategoryGridScreen(
            uiState = HomeUiState.Success(
                categoriesSummary = listOf(
                    CategoryOverview("Food", 42.0, 1, 100.0),
                    CategoryOverview("Coffee", 15.0, 1, 50.0)
                )
            ),
            onEvent = {},
            navTo = {}
        )
    }
}
