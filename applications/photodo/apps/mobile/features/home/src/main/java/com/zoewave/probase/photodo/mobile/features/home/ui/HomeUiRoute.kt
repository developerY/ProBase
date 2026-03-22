package com.zoewave.probase.photodo.mobile.features.home.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeUiRoute(
    onNavigateToCategoryGrid: () -> Unit,
    onNavigateToProject: (Long, String) -> Unit, // ✅ NEW: The warp-pipe to Level 3!
    modifier: Modifier = Modifier
) {
    // NOTE: You'll eventually pull this from your HomeViewModel!
    // For now, we use a mock list to build the UI perfectly.
    val urgentProjects = listOf(
        Pair(1001L, "PreFab Home Setup"),
        Pair(1005L, "Kitchen Remodel Phase 2")
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text("Overview") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 📈 1. Your Graphic & AI Placeholder
            Card(
                modifier = Modifier.fillMaxWidth().height(200.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "📈 Graphic & AI Agent Placeholder",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 📂 2. The standard drill-down button
            Button(
                onClick = onNavigateToCategoryGrid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View All Categories")
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ⭐ 3. The New "Urgent / Favorites" Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = "Jump Back In",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(urgentProjects) { project ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                // ✅ TRIGGER THE JUMP TO THE TASKS TAB!
                                onNavigateToProject(project.first, project.second)
                            },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = "Urgent", tint = MaterialTheme.colorScheme.tertiary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(project.second, style = MaterialTheme.typography.bodyLarge)
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = "Go")
                        }
                    }
                }
            }
        }
    }
}