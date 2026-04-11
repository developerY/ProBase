package com.zoewave.probase.seaweed.mobile.transaction.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.zoewave.probase.seaweed.model.navigation.SeaweedDestination
import java.util.Locale

@Composable
fun AddTransactionUiRoute(
    modifier: Modifier = Modifier,
    viewModel: AddTransactionViewModel = hiltViewModel(),
    navTo: (SeaweedDestination) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onBack()
        }
    }

    AddTransactionScreen(
        modifier = modifier,
        uiState = uiState,
        onEvent = { event ->
            if (event is AddTransactionUiEvent.BackClicked) {
                onBack()
            } else {
                viewModel.onEvent(event)
            }
        },
        navTo = navTo
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTransactionScreen(
    modifier: Modifier = Modifier,
    uiState: AddTransactionUiState,
    onEvent: (AddTransactionUiEvent) -> Unit,
    navTo: (SeaweedDestination) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Transaction") },
                navigationIcon = {
                    IconButton(onClick = { onEvent(AddTransactionUiEvent.BackClicked) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { navTo(SeaweedDestination.Camera) }) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Take Receipt Photo")
                    }
                }
            )
        },
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            uiState.receiptUri?.let { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = "Receipt",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )
            }
            OutlinedTextField(
                value = uiState.description,
                onValueChange = { onEvent(AddTransactionUiEvent.DescriptionChanged(it)) },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = uiState.amount,
                onValueChange = { onEvent(AddTransactionUiEvent.AmountChanged(it)) },
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = uiState.category,
                    onValueChange = { onEvent(AddTransactionUiEvent.CategoryChanged(it)) },
                    label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = { Icon(Icons.Default.Category, contentDescription = null) }
                )

                if (uiState.recentCategories.isNotEmpty()) {
                    Text("Recent", style = MaterialTheme.typography.labelMedium)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 8.dp)
                    ) {
                        items(uiState.recentCategories) { category ->
                            AssistChip(
                                onClick = { onEvent(AddTransactionUiEvent.CategoryChanged(category)) },
                                label = { Text(category) }
                            )
                        }
                    }
                }

                Text("Suggestions", style = MaterialTheme.typography.labelMedium)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val suggestions = listOf(
                        "Food" to Icons.Default.Fastfood,
                        "Coffee" to Icons.Default.Coffee,
                        "Transport" to Icons.Default.DirectionsBus,
                        "Shopping" to Icons.Default.ShoppingBag,
                        "Housing" to Icons.Default.Home,
                        "Health" to Icons.Default.LocalHospital
                    )
                    suggestions.forEach { (name, icon) ->
                        FilterChip(
                            selected = uiState.category == name,
                            onClick = { onEvent(AddTransactionUiEvent.CategoryChanged(name)) },
                            label = { Text(name) },
                            leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        )
                    }
                }
            }

            Button(
                onClick = { onEvent(AddTransactionUiEvent.SaveTransaction) },
                modifier = Modifier.fillMaxWidth()
            ) {
                val base = uiState.amount.toDoubleOrNull() ?: 0.0
                val tip = uiState.customTipAmount.toDoubleOrNull() ?: 0.0
                val total = base + tip
                if (total > 0 && tip > 0) {
                    Text("Save (Total: $${String.format(Locale.getDefault(), "%.2f", total)})")
                } else {
                    Text("Save")
                }
            }

            TextButton(
                onClick = { onEvent(AddTransactionUiEvent.ToggleTipWidget) },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(if (uiState.isTipWidgetVisible) "Hide Tip Options" else "Add Tip")
            }

            AnimatedVisibility(visible = uiState.isTipWidgetVisible) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Standard Tips", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(10, 15, 18, 20).forEach { percentage ->
                            FilterChip(
                                selected = uiState.tipPercentage == percentage,
                                onClick = { onEvent(AddTransactionUiEvent.SelectTipPercentage(percentage)) },
                                label = { Text("$percentage%") }
                            )
                        }
                        FilterChip(
                            selected = uiState.tipPercentage == null && uiState.customTipAmount.isNotEmpty(),
                            onClick = { onEvent(AddTransactionUiEvent.SelectTipPercentage(null)) },
                            label = { Text("None") }
                        )
                    }
                    OutlinedTextField(
                        value = uiState.customTipAmount,
                        onValueChange = { onEvent(AddTransactionUiEvent.CustomTipAmountChanged(it)) },
                        label = { Text("Tip Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            TextButton(
                onClick = { onEvent(AddTransactionUiEvent.ToggleSplitWidget) },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(if (uiState.isSplitWidgetVisible) "Hide Split Bill" else "Split Bill")
            }

            AnimatedVisibility(visible = uiState.isSplitWidgetVisible) {
                val base = uiState.amount.toDoubleOrNull() ?: 0.0
                val tip = uiState.customTipAmount.toDoubleOrNull() ?: 0.0
                val total = base + tip
                val perPerson = if (uiState.splitCount > 0) total / uiState.splitCount else total

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Split among", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        IconButton(onClick = { onEvent(AddTransactionUiEvent.SplitCountChanged(uiState.splitCount - 1)) }) {
                            Icon(Icons.Default.Remove, contentDescription = "Remove person")
                        }
                        Text(
                            text = "${uiState.splitCount} people",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { onEvent(AddTransactionUiEvent.SplitCountChanged(uiState.splitCount + 1)) }) {
                            Icon(Icons.Default.Add, contentDescription = "Add person")
                        }
                    }
                    if (uiState.splitCount > 1) {
                        Text(
                            text = "Each person pays: $${String.format(Locale.getDefault(), "%.2f", perPerson)}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AddTransactionScreenPreview() {
    MaterialTheme {
        AddTransactionScreen(
            uiState = AddTransactionUiState(
                amount = "42.00",
                category = "Food",
                description = "Lunch with friends",
                isTipWidgetVisible = true,
                tipPercentage = 15,
                customTipAmount = "6.30",
                isSplitWidgetVisible = true,
                splitCount = 3,
                recentCategories = listOf("Groceries", "Entertainment", "Gifts")
            ),
            onEvent = {},
            navTo = {}
        )
    }
}

@Preview(showBackground = true, name = "Empty State")
@Composable
private fun AddTransactionScreenEmptyPreview() {
    MaterialTheme {
        AddTransactionScreen(
            uiState = AddTransactionUiState(),
            onEvent = {},
            navTo = {}
        )
    }
}
