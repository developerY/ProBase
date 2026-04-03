package com.zoewave.probase.photodo.wear.features.project.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.tooling.preview.devices.WearDevices
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TitleCard
import com.zoewave.probase.photodo.data.util.loadAssetAsBitmap
import com.zoewave.probase.photodo.wear.features.project.ProjectWearUiModel
import java.util.Locale

@Composable
fun ProjectCard(
    project: ProjectWearUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var assetBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

    if (project.hasPhoto) {
        LaunchedEffect(project.id, project.hasPhoto) {
            android.util.Log.d("PhotoDoSync_UI", "ProjectCard: Loading primary photo for project ${project.id}")
            assetBitmap = loadAssetAsBitmap(context, "/photodo/sync_state", "photo_${project.id}_0")
            if (assetBitmap != null) {
                android.util.Log.d("PhotoDoSync_UI", "ProjectCard: Successfully loaded primary photo for project ${project.id}")
            } else {
                android.util.Log.w("PhotoDoSync_UI", "ProjectCard: Failed to load primary photo for project ${project.id}")
            }
        }
    }

    val titleBlock: @Composable RowScope.() -> Unit = {
        Text(project.name)
    }

    TitleCard(
        onClick = onClick,
        title = titleBlock,
        subtitle = {
            val budgetText = if (project.budget > 0) {
                "Budget: $${String.format(Locale.getDefault(), "%.2f", project.budget)}"
            } else {
                "No Budget"
            }
            Text(budgetText)
        },
        time = {
            project.dueDate?.let {
                Text(formatDate(it))
            }
        },
        modifier = modifier
    ) {
        if (assetBitmap != null) {
            Image(
                bitmap = assetBitmap!!.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .rotate(90f),
                contentScale = ContentScale.Crop
            )
        }
        Text("Spent: $${String.format(Locale.getDefault(), "%.2f", project.currentSpend)}")
    }
}

private fun formatDate(timeInMillis: Long): String {
    val formatter = java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault())
    return formatter.format(java.util.Date(timeInMillis))
}
