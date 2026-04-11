package com.zoewave.probase.photodo.mobile.features.home.ui.components.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.photodo.mobile.core.ui.theme.PhotoDoTheme
import com.zoewave.probase.photodo.mobile.features.home.ui.components.CategoryQuickJumpUiModel
import com.zoewave.probase.photodo.model.navigation.PhotoTodoRoute

/**
 * A compact, expressively rounded card for a single category quick-jump chip.
 */
@Composable
fun CategoryQuickJumpCard(
    model: CategoryQuickJumpUiModel,
    onEvent: (HomeEvent) -> Unit,
    navTo: (PhotoTodoRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = { navTo(PhotoTodoRoute.TasksList(categoryId = model.id, categoryName = model.name)) },
        modifier = modifier
            .width(130.dp) // Fixed width for compact row alignment
            .clip(RoundedCornerShape(20.dp)), // expressive round radius
        colors = CardDefaults.cardColors(
            containerColor = model.containerColor,
            // Assuming we calculate contentColor or just let Card handle it
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Icon & Category Name
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    imageVector = model.icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = model.name,
                    style = MaterialTheme.typography.titleSmall, // compact font size
                    maxLines = 1,
                    fontWeight = FontWeight.Bold
                )
            }

            // Progress details
            Column {
                Text(
                    text = "${model.totalProjects} Projects",
                    style = MaterialTheme.typography.labelSmall,
                )
                Text(
                    text = model.progressText,
                    style = MaterialTheme.typography.labelSmall,
                )

                // Tiny progress bar
                LinearProgressIndicator(
                    progress = { model.progressPercentage },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)), // Rounded ends
                    // color = contentColor,
                    // trackColor = contentColor.copy(alpha = 0.2f),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CategoryQuickJumpCardPreview() {
    PhotoDoTheme {
        CategoryQuickJumpCard(
            model = CategoryQuickJumpUiModel(
                id = 1L,
                name = "Nature",
                totalProjects = 12,
                progressText = "5/10 Tasks",
                progressPercentage = 0.5f,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                icon = Icons.Default.FolderSpecial
            ),
            onEvent = {},
            navTo = {}
        )
    }
}