package com.zoewave.probase.applications.journal.features.main.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zoewave.probase.applications.journal.model.JournalEntry
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalFullView(
    viewModel: JournalViewModel,
    onAddEntry: () -> Unit,
    onEditEntry: (JournalEntry) -> Unit
) {
    val entries by viewModel.journalEntries.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("My Journal") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddEntry) {
                Icon(Icons.Default.Add, contentDescription = "Add Entry")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(entries) { entry ->
                JournalItem(
                    entry = entry,
                    onClick = { onEditEntry(entry) }
                )
            }
        }
    }
}

@Composable
fun JournalItem(
    entry: JournalEntry,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = entry.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(entry.timestamp)),
                style = MaterialTheme.typography.bodySmall
            )
            if (entry.images.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    entry.images.take(5).forEach { _ ->
                        // Placeholder for image icons
                        Surface(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = MaterialTheme.shapes.extraSmall
                        ) {}
                    }
                    if (entry.images.size > 5) {
                        Text(text = "+${entry.images.size - 5}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
