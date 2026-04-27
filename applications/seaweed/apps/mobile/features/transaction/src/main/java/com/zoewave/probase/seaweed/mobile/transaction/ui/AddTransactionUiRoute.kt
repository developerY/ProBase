package com.zoewave.probase.seaweed.mobile.transaction.ui

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.zoewave.probase.core.ui.components.QuickExpenseBar
import com.zoewave.probase.features.payment.googlepay.GooglePayConfig
import com.zoewave.probase.features.payment.googlepay.ui.SeaweedGooglePayButton
import com.zoewave.probase.features.payment.stripe.ui.LocalStripeLauncher
import com.zoewave.probase.features.payment.stripe.ui.presentSeaweedPayment
import com.zoewave.probase.seaweed.features.spendingcontrol.domain.InterventionAction
import com.zoewave.probase.seaweed.features.spendingcontrol.domain.InterventionState
import com.zoewave.probase.seaweed.features.spendingcontrol.ui.InterventionDialog
import com.zoewave.probase.seaweed.mobile.transaction.R
import com.zoewave.probase.seaweed.model.SpendingType
import com.zoewave.probase.seaweed.model.navigation.SeaweedDestination
import kotlinx.coroutines.launch
import java.util.Locale
import com.zoewave.probase.core.ui.R as CoreUiR

@Composable
fun AddTransactionUiRoute(
    navTo: (SeaweedDestination) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddTransactionViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val interventionState by viewModel.spendingControlOrchestrator.interventionState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onBack()
        }
    }

    AddTransactionScreen(
        uiState = uiState,
        interventionState = interventionState,
        onEvent = { event ->
            when (event) {
                is AddTransactionUiEvent.BackClicked -> onBack()
                AddTransactionUiEvent.DebugAiClicked -> {
                    uiState.lastAiDebugInfo?.let { debug ->
                        navTo(
                            SeaweedDestination.SmartReceiptDebug(
                                rawResponse = debug.rawResponse,
                                logs = debug.logs,
                                engineUsed = debug.engineUsed
                            )
                        )
                    }
                }
                else -> viewModel.onEvent(event)
            }
        },
        resolveIntervention = { action ->
            scope.launch {
                viewModel.spendingControlOrchestrator.resolveIntervention(action)
                if (action == InterventionAction.Override) {
                    viewModel.onEvent(AddTransactionUiEvent.SaveTransaction)
                }
            }
        },
        navTo = navTo,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    uiState: AddTransactionUiState,
    interventionState: InterventionState?,
    onEvent: (AddTransactionUiEvent) -> Unit,
    resolveIntervention: (InterventionAction) -> Unit,
    navTo: (SeaweedDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.applications_seaweed_apps_mobile_features_transaction_add_title)) },
                navigationIcon = {
                    IconButton(onClick = { onEvent(AddTransactionUiEvent.BackClicked) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = stringResource(CoreUiR.string.cd_navigate_back)
                        )
                    }
                },
                actions = {
                    if (uiState.lastAiDebugInfo != null) {
                        IconButton(onClick = { onEvent(AddTransactionUiEvent.DebugAiClicked) }) {
                            Icon(
                                Icons.Default.BugReport, 
                                contentDescription = stringResource(R.string.applications_seaweed_apps_mobile_features_transaction_view_ai_debug), 
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    IconButton(onClick = { navTo(SeaweedDestination.Camera) }) {
                        Icon(
                            Icons.Default.CameraAlt, 
                            contentDescription = stringResource(R.string.applications_seaweed_apps_mobile_features_transaction_take_photo)
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            AddTransactionContent(
                uiState = uiState,
                onEvent = onEvent
            )

            if (uiState.showCaptureTypeSelection) {
                CaptureTypeSelectionSheet(
                    comment = uiState.userContextComment,
                    onCommentChanged = { onEvent(AddTransactionUiEvent.UserCommentChanged(it)) },
                    onReceiptSelected = { onEvent(AddTransactionUiEvent.SelectReceiptMode) },
                    onPurchaseSelected = { onEvent(AddTransactionUiEvent.SelectPurchaseMode(uiState.userContextComment)) },
                    onDismiss = { onEvent(AddTransactionUiEvent.CancelCaptureSelection) }
                )
            }

            if (uiState.isLoading) {
                AddTransactionLoadingOverlay()
            }
        }
    }

    interventionState?.let { state ->
        InterventionDialog(
            state = state,
            onAction = resolveIntervention,
            onDismiss = { resolveIntervention(InterventionAction.Cancel) }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun AddTransactionContent(
    uiState: AddTransactionUiState,
    onEvent: (AddTransactionUiEvent) -> Unit
) {
    val stripeLauncher = LocalStripeLauncher.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        uiState.receiptUri?.let { uri ->
            AsyncImage(
                model = uri,
                contentDescription = stringResource(R.string.applications_seaweed_apps_mobile_features_transaction_cd_receipt),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )
        }
        OutlinedTextField(
            value = uiState.description,
            onValueChange = { onEvent(AddTransactionUiEvent.DescriptionChanged(it)) },
            label = { Text(stringResource(R.string.applications_seaweed_apps_mobile_features_transaction_description)) },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = uiState.amount,
            onValueChange = { onEvent(AddTransactionUiEvent.AmountChanged(it)) },
            label = { Text(stringResource(R.string.applications_seaweed_apps_mobile_features_transaction_amount)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )

        if (uiState.transactionDate != null) {
            TransactionDateBadge(
                date = uiState.transactionDate,
                onClear = { onEvent(AddTransactionUiEvent.ClearTransactionDate) }
            )
        }

        QuickExpenseBar(
            onAdjustAmount = { delta -> onEvent(AddTransactionUiEvent.AdjustAmount(delta)) }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Importance:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            FilterChip(
                selected = uiState.importance == SpendingType.NEED,
                onClick = { onEvent(AddTransactionUiEvent.ImportanceChanged(SpendingType.NEED)) },
                label = { Text("Required") }
            )
            FilterChip(
                selected = uiState.importance == SpendingType.WANT,
                onClick = { onEvent(AddTransactionUiEvent.ImportanceChanged(SpendingType.WANT)) },
                label = { Text("Optional") }
            )
            
            Spacer(Modifier.weight(1f))

            IconButton(
                onClick = { onEvent(AddTransactionUiEvent.CaptureLocation) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Capture Location",
                    tint = if (uiState.latitude != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = uiState.category,
                onValueChange = { onEvent(AddTransactionUiEvent.CategoryChanged(it)) },
                label = { Text(stringResource(R.string.applications_seaweed_apps_mobile_features_transaction_category)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        onEvent(AddTransactionUiEvent.SetCategorySuggestionsVisible(focusState.isFocused))
                    },
                trailingIcon = { Icon(Icons.Default.Category, contentDescription = null) }
            )

            AnimatedVisibility(visible = uiState.isCategorySuggestionsVisible) {
                CategorySuggestions(
                    recentCategories = uiState.recentCategories,
                    selectedCategory = uiState.category,
                    onCategorySelected = { onEvent(AddTransactionUiEvent.CategoryChanged(it)) }
                )
            }
        }

        SmartPurchaseGuidance(
            onSave = { onEvent(AddTransactionUiEvent.SaveTransaction) },
            onStripePay = { stripeLauncher?.presentSeaweedPayment("pi_test_123_secret_abc") },
            stripeEnabled = stripeLauncher != null
        )

        SaveButton(
            amount = uiState.amount,
            tip = uiState.customTipAmount,
            onClick = { onEvent(AddTransactionUiEvent.SaveTransaction) }
        )

        TextButton(
            onClick = { onEvent(AddTransactionUiEvent.ToggleTipWidget) },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(
                text = if (uiState.isTipWidgetVisible) 
                    stringResource(R.string.applications_seaweed_apps_mobile_features_transaction_hide_tip) 
                else 
                    stringResource(R.string.applications_seaweed_apps_mobile_features_transaction_add_tip)
            )
        }

        AnimatedVisibility(visible = uiState.isTipWidgetVisible) {
            TipWidget(
                tipPercentage = uiState.tipPercentage,
                customTipAmount = uiState.customTipAmount,
                onTipSelected = { onEvent(AddTransactionUiEvent.SelectTipPercentage(it)) },
                onCustomTipChanged = { onEvent(AddTransactionUiEvent.CustomTipAmountChanged(it)) }
            )
        }

        TextButton(
            onClick = { onEvent(AddTransactionUiEvent.ToggleSplitWidget) },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(
                text = if (uiState.isSplitWidgetVisible) 
                    stringResource(R.string.applications_seaweed_apps_mobile_features_transaction_hide_split) 
                else 
                    stringResource(R.string.applications_seaweed_apps_mobile_features_transaction_split_bill)
            )
        }

        AnimatedVisibility(visible = uiState.isSplitWidgetVisible) {
            SplitWidget(
                amount = uiState.amount,
                tip = uiState.customTipAmount,
                splitCount = uiState.splitCount,
                onSplitCountChanged = { onEvent(AddTransactionUiEvent.SplitCountChanged(it)) }
            )
        }
    }
}

@Composable
private fun TransactionDateBadge(
    date: Long,
    onClear: () -> Unit
) {
    val dateString = java.text.SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Event,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = "Date: $dateString",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.weight(1f))
        IconButton(
            onClick = onClear,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Clear Date",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategorySuggestions(
    recentCategories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (recentCategories.isNotEmpty()) {
            Text(
                text = stringResource(R.string.applications_seaweed_apps_mobile_features_transaction_recent), 
                style = MaterialTheme.typography.labelMedium
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(recentCategories) { category ->
                    AssistChip(
                        onClick = { onCategorySelected(category) },
                        label = { Text(category) }
                    )
                }
            }
        }

        Text(
            text = stringResource(R.string.applications_seaweed_apps_mobile_features_transaction_suggestions), 
            style = MaterialTheme.typography.labelMedium
        )
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
                val isSelected = selectedCategory.equals(name, ignoreCase = true)
                FilterChip(
                    selected = isSelected,
                    onClick = { onCategorySelected(name) },
                    label = { Text(name) },
                    leadingIcon = {
                        Icon(
                            icon,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun SmartPurchaseGuidance(
    onSave: () -> Unit,
    onStripePay: () -> Unit,
    stripeEnabled: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Smart Purchase Guidance",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        
        if (GooglePayConfig.IS_ENABLED) {
            SeaweedGooglePayButton(
                onClick = onSave,
                enabled = false
            )
        }

        Button(
            onClick = onStripePay,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            enabled = stripeEnabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF635BFF),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Pay with Stripe", fontWeight = FontWeight.Bold)
        }
        
        Text(
            text = "AI will automatically verify this purchase against your budget and rewards.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SaveButton(
    amount: String,
    tip: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        val base = amount.toDoubleOrNull() ?: 0.0
        val tipValue = tip.toDoubleOrNull() ?: 0.0
        val total = base + tipValue
        if (total > 0 && tipValue > 0) {
            Text(
                text = stringResource(
                    R.string.applications_seaweed_apps_mobile_features_transaction_save_total, 
                    String.format(Locale.getDefault(), "%.2f", total)
                )
            )
        } else {
            Text(stringResource(R.string.applications_seaweed_apps_mobile_features_transaction_save))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TipWidget(
    tipPercentage: Int?,
    customTipAmount: String,
    onTipSelected: (Int?) -> Unit,
    onCustomTipChanged: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.applications_seaweed_apps_mobile_features_transaction_standard_tips), 
            style = MaterialTheme.typography.labelMedium
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(10, 15, 18, 20).forEach { percentage ->
                FilterChip(
                    selected = tipPercentage == percentage,
                    onClick = { onTipSelected(percentage) },
                    label = { Text("$percentage%") }
                )
            }
            FilterChip(
                selected = tipPercentage == null && customTipAmount.isNotEmpty(),
                onClick = { onTipSelected(null) },
                label = { Text(stringResource(R.string.applications_seaweed_apps_mobile_features_transaction_none)) }
            )
        }
        OutlinedTextField(
            value = customTipAmount,
            onValueChange = onCustomTipChanged,
            label = { Text(stringResource(R.string.applications_seaweed_apps_mobile_features_transaction_tip_amount)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun SplitWidget(
    amount: String,
    tip: String,
    splitCount: Int,
    onSplitCountChanged: (Int) -> Unit
) {
    val base = amount.toDoubleOrNull() ?: 0.0
    val tipValue = tip.toDoubleOrNull() ?: 0.0
    val total = base + tipValue
    val perPerson = if (splitCount > 0) total / splitCount else total

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.applications_seaweed_apps_mobile_features_transaction_split_among), 
            style = MaterialTheme.typography.labelMedium
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconButton(onClick = { onSplitCountChanged(splitCount - 1) }) {
                Icon(Icons.Default.Remove, contentDescription = null)
            }
            Text(
                text = stringResource(R.string.applications_seaweed_apps_mobile_features_transaction_people_count, splitCount),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { onSplitCountChanged(splitCount + 1) }) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
        if (splitCount > 1) {
            Text(
                text = stringResource(
                    R.string.applications_seaweed_apps_mobile_features_transaction_each_pays, 
                    stringResource(R.string.applications_seaweed_apps_mobile_features_transaction_currency_format, String.format(Locale.getDefault(), "%.2f", perPerson))
                ),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun AddTransactionLoadingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.applications_seaweed_apps_mobile_features_transaction_ai_analyzing), 
                style = MaterialTheme.typography.bodyMedium
            )
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
            interventionState = null,
            onEvent = {},
            resolveIntervention = {},
            navTo = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AddTransactionScreenInterventionPreview() {
    MaterialTheme {
        AddTransactionScreen(
            uiState = AddTransactionUiState(
                amount = "60.00",
                category = "Dining",
                description = "Expensive Dinner"
            ),
            interventionState = InterventionState(
                merchantName = "Starbucks",
                amountCents = 6000,
                categoryId = "dining_id",
                envelopeId = "dining_env",
                reason = "Dining limit exceeded"
            ),
            onEvent = {},
            resolveIntervention = {},
            navTo = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CaptureTypeSelectionSheetPreview() {
    MaterialTheme {
        CaptureTypeSelectionSheet(
            comment = "Lunch with friends",
            onCommentChanged = {},
            onReceiptSelected = {},
            onPurchaseSelected = {},
            onDismiss = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureTypeSelectionSheet(
    comment: String,
    onCommentChanged: (String) -> Unit,
    onReceiptSelected: () -> Unit,
    onPurchaseSelected: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Analyze Photo with AI",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Choose how you want the AI to process this image.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onReceiptSelected,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                ) {
                    Text("Receipt")
                }
                Button(
                    onClick = onPurchaseSelected,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Purchase")
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Add context for Purchase (Optional)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                OutlinedTextField(
                    value = comment,
                    onValueChange = onCommentChanged,
                    placeholder = { Text("e.g. Buying a new monitor for work...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                Text(
                    text = "Providing details helps the AI better categorize and describe the purchase.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
