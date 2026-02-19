package com.zoewave.probase.features.health.ui.sessions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.records.ExerciseSessionRecord
import com.zoewave.probase.features.health.ui.HealthEvent

@Composable
fun SessionsTab(
    sessions: List<ExerciseSessionRecord>,
    onEvent: (HealthEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSession by remember { mutableStateOf<ExerciseSessionRecord?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = "Recent Sessions",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (sessions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No recent activities found.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(sessions) { session ->
                    // Make sure you updated SessionCard to accept clicks!
                    SessionCard(
                        session = session,
                        onClick = { selectedSession = session }
                    )
                }
            }
        }
    }

    // Show Dialog if a session is selected
    if (selectedSession != null) {
        SessionDetailDialog(
            session = selectedSession!!,
            onDismiss = { selectedSession = null },
            onDelete = { uid ->
                onEvent(HealthEvent.DeleteSession(uid))
            }
        )
    }
}