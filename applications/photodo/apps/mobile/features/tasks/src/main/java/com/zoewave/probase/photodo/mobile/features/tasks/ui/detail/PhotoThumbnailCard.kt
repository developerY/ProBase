package com.zoewave.probase.photodo.mobile.features.tasks.ui.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.zoewave.probase.applications.photodo.db.entity.PhotoEntity
import com.zoewave.probase.applications.photodo.db.entity.TaskEntity
import com.zoewave.probase.photodo.mobile.core.ui.theme.PhotoDoTheme
import com.zoewave.probase.photodo.mobile.features.tasks.R
import com.zoewave.probase.photodo.model.navigation.PhotoTodoRoute

@Composable
fun PhotoThumbnailCard(
    photo: PhotoEntity,
    onEvent: (TaskDetailEvent) -> Unit,
    navTo: (PhotoTodoRoute?) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(120.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {
        AsyncImage(
            model = photo.photoUri,
            contentDescription = stringResource(R.string.applications_photodo_apps_mobile_features_tasks_detail_context_photo_content_desc),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // 🛑 2. Delete Button Overlay (Layer 2 - Top Right)
        IconButton(
            onClick = { onEvent(TaskDetailEvent.OnDeletePhoto(photo.photoId)) },
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Surface(
                shape = RoundedCornerShape(percent = 50),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = stringResource(R.string.applications_photodo_apps_mobile_features_tasks_detail_delete_photo_content_desc),
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
    }
}

@Composable
fun TaskItemRow(
    task: TaskEntity,
    onEvent: (TaskDetailEvent) -> Unit,
    navTo: (PhotoTodoRoute?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = task.isChecked,
            onCheckedChange = { isChecked ->
                onEvent(TaskDetailEvent.OnItemCheckedChange(task, isChecked))
            }
        )
        Text(
            text = task.text,
            style = MaterialTheme.typography.bodyLarge,
            textDecoration = if (task.isChecked) TextDecoration.LineThrough else TextDecoration.None,
            color = if (task.isChecked) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = { onEvent(TaskDetailEvent.OnDeleteItem(task)) }) {
            Icon(
                Icons.Default.Close,
                contentDescription = stringResource(R.string.applications_photodo_apps_mobile_features_tasks_detail_delete_task_content_desc),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PhotoThumbnailCardPreview() {
    PhotoDoTheme {
        PhotoThumbnailCard(
            photo = PhotoEntity(photoId = 1, projectId = 1, photoUri = "content://media/external/images/media/1"),
            onEvent = {},
            navTo = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TaskItemRowPreview() {
    PhotoDoTheme {
        TaskItemRow(
            task = TaskEntity(taskId = 1, projectId = 1, text = "Example Task", isChecked = false),
            onEvent = {},
            navTo = {}
        )
    }
}

