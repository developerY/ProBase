package com.zoewave.probase.photodo.mobile.features.tasks.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.photodo.mobile.core.ui.theme.PhotoDoTheme
import com.zoewave.probase.photodo.mobile.features.tasks.ui.TasksEvent
import com.zoewave.probase.photodo.mobile.features.tasks.ui.state.TasksUiState

@Composable
fun QuickProjectContent(
    uiState: TasksUiState,
    onEvent: (TasksEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Dynamic title based on override
        val titleText = if (uiState.quickProjectCategoryOverride != null) "Home Project" else "Quick Project"
        val subtitlePrefix = uiState.quickProjectCategoryOverride ?: uiState.categoryName

        // --- HEADER ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = titleText,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
            IconButton(onClick = { onEvent(TasksEvent.OnDismissBottomSheet) }) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        // --- ITEMS ---
        QuickProjectItem(
            title = "Quick Fix",
            subtitle = "$subtitlePrefix • $50 Budget",
            icon = Icons.Default.Build,
            onClick = { onEvent(TasksEvent.OnAddQuickProject("fix", subtitlePrefix, 50.0)) }
        )

        QuickProjectItem(
            title = "Quick Buy",
            subtitle = "$subtitlePrefix • $100 Budget",
            icon = Icons.Default.ShoppingCart,
            onClick = { onEvent(TasksEvent.OnAddQuickProject("buy", subtitlePrefix, 100.0)) }
        )

        QuickProjectItem(
            title = "Quick Find",
            subtitle = "$subtitlePrefix • Search & Discover",
            icon = Icons.Default.Search,
            onClick = { onEvent(TasksEvent.OnAddQuickProject("find", subtitlePrefix, 0.0)) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickProjectBottomSheet(
    uiState: TasksUiState,
    onEvent: (TasksEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = { onEvent(TasksEvent.OnDismissBottomSheet) },
        modifier = modifier,
        sheetState = sheetState
    ) {
        QuickProjectContent(uiState = uiState, onEvent = onEvent)
    }
}

@Composable
private fun QuickProjectItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun QuickProjectBottomSheetPreview() {
    PhotoDoTheme {
        Surface {
            QuickProjectContent(
                uiState = TasksUiState(categoryName = "Work"),
                onEvent = {}
            )
        }
    }
}
