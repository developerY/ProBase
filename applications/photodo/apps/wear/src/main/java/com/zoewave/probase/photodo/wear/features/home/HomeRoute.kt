package com.zoewave.probase.photodo.wear.features.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TitleCard

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.zoewave.probase.photodo.data.util.loadAssetAsBitmap

@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    onCategoryClick: (Long, String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        modifier = modifier,
        uiState = uiState,
        onCategoryClick = onCategoryClick,
        onRequestSync = viewModel::requestSync
    )
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    onCategoryClick: (Long, String) -> Unit,
    onRequestSync: () -> Unit
) {
    val listState = rememberScalingLazyListState()

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (uiState) {
            HomeUiState.Loading -> {
                CircularProgressIndicator()
            }
            HomeUiState.Empty -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Open PhotoDo on phone to sync",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onRequestSync,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Sync"
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sync Now")
                    }
                }
            }
            is HomeUiState.Success -> {
                ScalingLazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 32.dp, bottom = 32.dp, start = 8.dp, end = 8.dp)
                ) {
                    item {
                        ListHeader {
                            Text("Categories")
                        }
                    }
                    items(uiState.categories) { category ->
                        CategoryItem(
                            category = category,
                            onClick = { onCategoryClick(category.id, category.name) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryItem(
    category: CategoryWearUiModel,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    var categoryBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

    if (category.hasPhoto) {
        LaunchedEffect(category.id) {
            categoryBitmap = loadAssetAsBitmap(context, "/photodo/sync_state", "category_${category.id}")
        }
    }

    val titleBlock: @Composable RowScope.() -> Unit = {
        Text(category.name)
    }

    TitleCard(
        onClick = onClick,
        title = titleBlock,
        subtitle = {
            Text("${category.completedTasks}/${category.totalTasks} tasks")
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        if (categoryBitmap != null) {
            Image(
                bitmap = categoryBitmap!!.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                contentScale = ContentScale.Crop
            )
        }
    }
}
