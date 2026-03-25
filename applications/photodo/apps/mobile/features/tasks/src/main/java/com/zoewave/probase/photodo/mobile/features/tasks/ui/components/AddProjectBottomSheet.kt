package com.zoewave.probase.photodo.mobile.features.tasks.ui.components
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.photodo.model.navigation.PhotoTodoRoute
import com.zoewave.probase.photodo.mobile.features.tasks.ui.TasksEvent
import com.zoewave.probase.photodo.mobile.features.tasks.ui.state.TaskDraftState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProjectBottomSheet(
    uiState: TaskDraftState,
    onEvent: (TasksEvent) -> Unit,
    navTo: (PhotoTodoRoute?) -> Unit, // ✅ Standardized Navigation Channel
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    // Inside your Bottom Sheet or Add Task dialog:
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDueDate by remember { mutableStateOf<Long?>(null) }

    // Auto-focus the text field when the sheet opens
    val focusRequester = remember { FocusRequester() }

    ModalBottomSheet(
        onDismissRequest = { onEvent(TasksEvent.OnDismissBottomSheet) },
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp), // Extra padding for the navigation bar
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row {
                Text(
                    text = "New Project",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = "Set Due Date")
                }
            }

            OutlinedTextField(
                value = uiState.listTitle,
                onValueChange = { onEvent(TasksEvent.OnDraftTitleChanged(it)) },
                label = { Text("Project Name") },
                placeholder = { Text("e.g., Kitchen Remodel") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (uiState.listTitle.isNotBlank()) {
                            onEvent(TasksEvent.OnSaveDraftClicked)
                        }
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { onEvent(TasksEvent.OnDismissBottomSheet) }) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {

                        onEvent(TasksEvent.OnSaveDraftClicked)
                        //onEvent(TaskDetailEvent.OnAddItemClicked(text, selectedDueDate))
                              },
                    enabled = uiState.listTitle.isNotBlank()
                ) {
                    Text("Create")
                }
            }
        }

        // Request focus once the sheet enters the composition
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

        if (showDatePicker) {
            PhotoDoDatePicker(
                onDateSelected = { timestamp ->
                    selectedDueDate = timestamp
                },
                onDismiss = { showDatePicker = false }
            )
        }
    }
}

@Preview
@Composable
private fun AddProjectBottomSheetPreview() {
    MaterialTheme {
        // Note: ModalBottomSheet won't render perfectly in a standard Preview
        // without a parent, but this gives you a quick look at the layout!
        AddProjectBottomSheet(
            uiState = TaskDraftState(),
            onEvent = {},
            navTo = {}
        )
    }
}