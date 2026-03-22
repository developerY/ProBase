package com.zoewave.probase.photodo.mobile.features.tasks.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zoewave.photodo.model.navigation.PhotoTodoRoute
import com.zoewave.probase.photodo.mobile.features.tasks.ui.TaskItemUiModel
import com.zoewave.probase.photodo.mobile.features.tasks.ui.TasksEvent

@Composable
fun TaskRow(
    uiState: TaskItemUiModel,
    onEvent: (TasksEvent) -> Unit,
    onNav: (PhotoTodoRoute?) -> Unit, // ✅ Standardized Navigation Channel
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = uiState.title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Checkbox(
                checked = uiState.isCompleted,
                onCheckedChange = { isChecked ->
                    onEvent(TasksEvent.OnTaskToggled(uiState.id, isChecked))
                }
            )
        }
    }
}
