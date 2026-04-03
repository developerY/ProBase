package com.zoewave.probase.photodo.wear.ui.components

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
import com.zoewave.probase.photodo.wear.ui.theme.PhotoDoWearTheme
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
        LaunchedEffect(project.id) {
            assetBitmap = loadAssetAsBitmap(context, "/photodo/sync_state", "photo_${project.id}")
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
                modifier = Modifier.size(48.dp),
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

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun ProjectCardPreview() {
    PhotoDoWearTheme {
        ProjectCard(
            project = ProjectWearUiModel(
                id = 1,
                name = "Project Alpha",
                budget = 1000.0,
                currentSpend = 250.0,
                dueDate = System.currentTimeMillis() + 86400000,
                isUrgent = false,
                progress = 0.25f
            ),
            onClick = {}
        )
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun ProjectCardNoBudgetPreview() {
    PhotoDoWearTheme {
        PhotoDoWearTheme {
            ProjectCard(
                project = ProjectWearUiModel(
                    id = 2,
                    name = "Quick Tasks",
                    budget = 0.0,
                    currentSpend = 0.0,
                    dueDate = null,
                    isUrgent = true,
                    progress = 0.5f
                ),
                onClick = {}
            )
        }
    }
}
