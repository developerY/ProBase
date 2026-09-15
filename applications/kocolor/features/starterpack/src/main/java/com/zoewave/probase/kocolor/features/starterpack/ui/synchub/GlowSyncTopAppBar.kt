package com.zoewave.probase.kocolor.features.starterpack.ui.synchub

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlowSyncTopAppBar(
    title: String = "Discover Cosmetics",
    query: String,
    onQueryChange: (String) -> Unit,
    onBack: () -> Unit
) {
    val serifFont = FontFamily.Serif
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            fontFamily = serifFont,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(end = 16.dp)
        )
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text(androidx.compose.ui.res.stringResource(com.zoewave.probase.kocolor.features.starterpack.R.string.applications_kocolor_features_starterpack_search_products), fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.LightGray.copy(alpha = 0.3f),
                focusedContainerColor = Color(0xFFF5F5F5),
                unfocusedContainerColor = Color(0xFFF5F5F5)
            ),
            singleLine = true
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GlowSyncTopAppBarPreview() {
    MaterialTheme {
        GlowSyncTopAppBar(
            query = "",
            onQueryChange = {},
            onBack = {}
        )
    }
}
