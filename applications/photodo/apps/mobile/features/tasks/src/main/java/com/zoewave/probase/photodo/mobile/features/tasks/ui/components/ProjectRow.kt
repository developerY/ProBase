package com.zoewave.probase.photodo.mobile.features.tasks.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.photodo.mobile.features.tasks.ui.state.ProjectListUiModel


@Composable
fun ProjectRow(
    project: ProjectListUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() } // ✅ This makes the whole row clickable!
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = project.title,
                    style = MaterialTheme.typography.titleMedium
                )
                if (project.categoryName != null) {
                    Text(
                        text = project.categoryName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // A visual cue that tapping this navigates deeper
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Open Project",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true, name = "With Category")
@Composable
private fun ProjectRowWithCategoryPreview() {
    MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            ProjectRow(
                project = ProjectListUiModel(
                    id = 1001L,
                    title = "PreFab Home Setup",
                    categoryName = "Real Estate"
                ),
                onClick = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Without Category")
@Composable
private fun ProjectRowWithoutCategoryPreview() {
    MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            ProjectRow(
                project = ProjectListUiModel(
                    id = 1002L,
                    title = "AshBike App Architecture",
                    categoryName = null // Simulating an uncategorized list
                ),
                onClick = {}
            )
        }
    }
}