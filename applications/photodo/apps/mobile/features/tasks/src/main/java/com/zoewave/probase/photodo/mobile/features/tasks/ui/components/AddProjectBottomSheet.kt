package com.zoewave.probase.photodo.mobile.features.tasks.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.applications.photodo.db.model.quickTemplates
import com.zoewave.probase.core.ui.components.QuickExpenseBar
import com.zoewave.probase.photodo.mobile.core.ui.theme.PhotoDoTheme
import com.zoewave.probase.photodo.mobile.features.tasks.R
import com.zoewave.probase.photodo.mobile.features.tasks.ui.TasksEvent
import com.zoewave.probase.photodo.mobile.features.tasks.ui.state.TaskDraftState
import com.zoewave.probase.photodo.model.navigation.PhotoTodoRoute
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProjectBottomSheet(
    uiState: TaskDraftState,
    onEvent: (TasksEvent) -> Unit,
    navTo: (PhotoTodoRoute?) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showDatePicker by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = { onEvent(TasksEvent.OnDismissBottomSheet) },
        modifier = modifier,
        sheetState = sheetState
    ) {
        AddProjectBottomSheetContent(
            uiState = uiState,
            onEvent = onEvent,
            onShowDatePicker = { showDatePicker = true }
        )

        // 🚀 ONLY ONE DATE PICKER REMAINS
        if (showDatePicker) {
            PhotoDoDatePicker(
                onDateSelected = { timestamp ->
                    onEvent(TasksEvent.OnDraftDueDateChanged(timestamp))
                },
                onDismiss = { showDatePicker = false }
            )
        }
    }
}

@Composable
fun AddProjectBottomSheetContent(
    uiState: TaskDraftState,
    onEvent: (TasksEvent) -> Unit,
    onShowDatePicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 24.dp)
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // --- HEADER ---
        val hasDueDate = uiState.dueDateMillis != null
        val themeColor = if (uiState.isFromAi) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
        val containerColor = if (uiState.isFromAi) MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (uiState.isFromAi) "AI Smart Draft" else stringResource(R.string.applications_photodo_apps_mobile_features_tasks_new_project),
                style = MaterialTheme.typography.headlineSmall,
                color = themeColor
            )

            IconButton(
                onClick = onShowDatePicker,
                colors = IconButtonDefaults.iconButtonColors(
                    // 🚀 Color changes so they know it is active!
                    contentColor = if (hasDueDate) themeColor else MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Icon(Icons.Default.CalendarMonth, contentDescription = stringResource(R.string.applications_photodo_apps_mobile_features_tasks_detail_set_due_date_content_desc))
            }
        }

        // --- QUICK PICK SECTION ---
        Text(
            text = "Quick Pick",
            style = MaterialTheme.typography.labelMedium,
            color = themeColor
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            quickTemplates.forEach { template ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickable {
                                onEvent(TasksEvent.OnDraftTitleChanged(template.title))
                                onEvent(TasksEvent.OnDraftBudgetChanged(template.defaultBudget.toInt().toString()))
                            },
                        color = if (uiState.isFromAi) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = template.icon,
                                contentDescription = template.title,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Text(
                        text = template.title.split(" ").firstOrNull() ?: template.title,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 🚀 ACTUAL DATE FORMATTER: Turns 1709234000000 into "Oct 24, 2026"
        if (hasDueDate) {
            val formatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
            val dateString = formatter.format(Date(uiState.dueDateMillis!!))

            Text(
                text = stringResource(R.string.applications_photodo_apps_mobile_features_tasks_detail_due_date, dateString),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // --- TITLE FIELD ---
        OutlinedTextField(
            value = uiState.listTitle,
            onValueChange = { onEvent(TasksEvent.OnDraftTitleChanged(it)) },
            label = { Text(stringResource(R.string.applications_photodo_apps_mobile_features_tasks_detail_project_name_label)) },
            placeholder = { Text(stringResource(R.string.applications_photodo_apps_mobile_features_tasks_detail_project_name_placeholder)) },
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
        )

        // --- BUDGET FIELD ---
        OutlinedTextField(
            value = uiState.budgetInput,
            onValueChange = { onEvent(TasksEvent.OnDraftBudgetChanged(it)) },
            label = { Text(stringResource(R.string.applications_photodo_apps_mobile_features_tasks_detail_total_budget_optional_label)) },
            placeholder = { Text(stringResource(R.string.applications_photodo_apps_mobile_features_tasks_detail_expense_amount_placeholder)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.AttachMoney,
                    contentDescription = stringResource(R.string.applications_photodo_apps_mobile_features_tasks_detail_currency_content_desc),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (uiState.listTitle.isNotBlank()) {
                        onEvent(TasksEvent.OnSaveDraftClicked)
                    }
                }
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // --- QUICK BUDGET SECTION ---
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Adjust Budget",
                style = MaterialTheme.typography.labelMedium,
                color = themeColor
            )
            QuickExpenseBar(
                onAdjustAmount = { adjustment ->
                    onEvent(TasksEvent.OnAdjustDraftBudget(adjustment)) 
                }
            )
        }

        // --- BUTTONS ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = { onEvent(TasksEvent.OnDismissBottomSheet) }
            ) {
                Text(stringResource(R.string.applications_photodo_apps_mobile_features_tasks_cancel_button))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { onEvent(TasksEvent.OnSaveDraftClicked) },
                enabled = uiState.listTitle.isNotBlank(),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = themeColor)
            ) {
                Text(stringResource(R.string.applications_photodo_apps_mobile_features_tasks_detail_create_button))
            }
        }
    }

    LaunchedEffect(Unit) {
        delay(100)
        focusRequester.requestFocus()
    }
}

@Preview(showBackground = true)
@Composable
private fun AddProjectBottomSheetPreview() {
    PhotoDoTheme {
        Surface {
            AddProjectBottomSheetContent(
                uiState = TaskDraftState(),
                onEvent = {},
                onShowDatePicker = {}
            )
        }
    }
}
