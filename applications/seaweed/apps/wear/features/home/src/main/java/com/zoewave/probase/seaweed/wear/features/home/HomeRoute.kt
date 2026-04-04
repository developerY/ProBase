package com.zoewave.probase.seaweed.wear.features.home

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material3.*
import com.zoewave.probase.seaweed.wear.features.home.HomeUiState
import com.zoewave.probase.seaweed.wear.features.home.HomeViewModel
import java.util.Locale

@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    onTransactionsClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        modifier = modifier,
        uiState = uiState,
        onTransactionsClick = onTransactionsClick,
        onAddClick = { viewModel.addRandomTransaction() }
    )
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    onTransactionsClick: () -> Unit,
    onAddClick: () -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (uiState) {
            HomeUiState.Loading -> {
                CircularProgressIndicator()
            }
            is HomeUiState.Success -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Balance",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = "$${String.format(Locale.getDefault(), "%.2f", uiState.totalBalance)}",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = onTransactionsClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Recent")
                    }
                    
                    Button(
                        onClick = onAddClick,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("Add Random")
                    }
                }
            }
        }
    }
}
