package com.zoewave.probase.photodo.mobile.features.tasks.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.xr.glimmer.Card
import androidx.xr.glimmer.GlimmerTheme
import androidx.xr.glimmer.Icon
import androidx.xr.glimmer.Text
import androidx.xr.glimmer.VoiceInputIndicator
import androidx.xr.glimmer.stack.VerticalStack
import androidx.xr.glimmer.stack.items
import androidx.xr.glimmer.surface
import com.zoewave.probase.applications.photodo.db.entity.ProjectDetails
import com.zoewave.probase.applications.photodo.db.entity.ProjectEntity

/**
 * A Glimmer-optimized Task Detail screen for AI glasses.
 * 
 * Features high-contrast checklist and a Voice Input Indicator for Gemini Live.
 */
@Composable
fun ProjectedTaskDetailScreen(
    uiState: TaskDetailUiState,
    isAiActive: Boolean,
    aiAudioLevel: () -> Float,
    isCapturing: Boolean = false,
    onToggleTask: (Long, Boolean) -> Unit
) {
    val successState = uiState.loadState as? DetailLoadState.Success
    val tasks = successState?.projectDetails?.tasks ?: emptyList()
    val projectName = successState?.projectDetails?.project?.name ?: "Project Details"

    Box(
        modifier = Modifier
            .surface()
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = projectName,
                        style = GlimmerTheme.typography.titleMedium,
                        color = GlimmerTheme.colors.primary
                    )
                    if (isCapturing) {
                        Text(
                            text = "Capturing Photo...",
                            style = GlimmerTheme.typography.caption,
                            color = GlimmerTheme.colors.secondary
                        )
                    }
                }
                
                if (isAiActive) {
                    VoiceInputIndicator(
                        level = aiAudioLevel,
                        indicatorColor = GlimmerTheme.colors.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // Task List
            if (tasks.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No tasks in this project.",
                        style = GlimmerTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            } else {
                VerticalStack(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    items(tasks, key = { it.taskId }) { task ->
                        Card(
                            onClick = { onToggleTask(task.taskId, !task.isChecked) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .itemDecoration(GlimmerTheme.shapes.medium),
                            title = {
                                Text(
                                    text = task.text,
                                    color = if (task.isChecked) Color.White.copy(alpha = 0.5f) else Color.White
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (task.isChecked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (task.isChecked) GlimmerTheme.colors.primary else Color.White
                                )
                            },
                            content = {}
                        )
                    }
                }
            }
            
            // Footer Info
            Text(
                text = "Talk to Gemini to manage tasks hands-free.",
                style = GlimmerTheme.typography.titleSmall,
                color = Color.White.copy(alpha = 0.4f),
                modifier = Modifier.padding(24.dp)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000, widthDp = 480, heightDp = 480)
@Composable
private fun ProjectedTaskDetailScreenPreview() {
    GlimmerTheme {
        ProjectedTaskDetailScreen(
            uiState = TaskDetailUiState(
                loadState = DetailLoadState.Success(
                    ProjectDetails(
                        project = ProjectEntity(projectId = 1, categoryId = 1, name = "Kitchen Renovation"),
                        tasks = listOf(
                            com.zoewave.probase.applications.photodo.db.entity.TaskEntity(taskId = 1, projectId = 1, text = "Paint Walls", isChecked = true),
                            com.zoewave.probase.applications.photodo.db.entity.TaskEntity(taskId = 2, projectId = 1, text = "Install Cabinets", isChecked = false)
                        ),
                        photos = emptyList(),
                        expenses = emptyList()
                    )
                )
            ),
            isAiActive = true,
            aiAudioLevel = { 0.5f },
            onToggleTask = { _, _ -> }
        )
    }
}
