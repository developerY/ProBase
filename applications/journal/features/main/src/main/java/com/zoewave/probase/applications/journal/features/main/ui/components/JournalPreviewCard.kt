package com.zoewave.probase.applications.journal.features.main.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.zoewave.probase.applications.journal.features.main.ui.JournalViewModel

@Composable
fun JournalPreviewCard(
    viewModel: JournalViewModel,
    onClick: () -> Unit
) {
    val entries by viewModel.journalEntries.collectAsState()
    val lastEntry = entries.firstOrNull()

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Recent Journal",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (lastEntry != null) {
                Text(
                    text = lastEntry.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = lastEntry.content,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Text(
                    text = "No entries yet. Start journaling today!",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
