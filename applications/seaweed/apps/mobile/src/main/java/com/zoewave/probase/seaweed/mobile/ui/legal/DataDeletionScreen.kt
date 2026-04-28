package com.zoewave.probase.seaweed.mobile.ui.legal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataDeletionScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Data Deletion", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "At Seaweed, we take your privacy seriously. Because our app is designed to function locally without external accounts, we do not have a centralized database containing your personal information.",
                style = MaterialTheme.typography.bodyLarge
            )

            DeletionSection(
                title = "1. Personal Content (Local Data)",
                content = "All of your transactions, bills, categories, budget envelopes, and receipt images are stored entirely on your device.\n\n" +
                        "• In-App Deletion: Delete individual items directly in the app.\n" +
                        "• Complete Removal: Uninstalling the app wipes the local database automatically."
            )

            DeletionSection(
                title = "2. Anonymized Analytics Deletion",
                content = "We use anonymous IDs to distinguish devices for stability monitoring. To request deletion of this data:\n\n" +
                        "1. Locate your App Instance ID in Settings > About.\n" +
                        "2. Email Developer@ZoeWave.com with this ID.\n\n" +
                        "We will then manually scrub all records associated with that ID from our analytics console."
            )

            RetentionTable()

            Text(
                "Questions? Reach out to Siamak Ashrafi at Developer@ZoeWave.com",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DeletionSection(title: String, content: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            lineHeight = 22.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun RetentionTable() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Retention Policy", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            
            RetentionRow("Financial Data", "Deleted immediately on uninstall")
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            RetentionRow("Receipt Images", "Deleted immediately on uninstall")
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            RetentionRow("Crash Logs", "Scrubbed within 90 days")
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            RetentionRow("Analytics IDs", "Kept 14 months unless requested")
        }
    }
}

@Composable
private fun RetentionRow(type: String, policy: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(type, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Text(policy, style = MaterialTheme.typography.bodySmall, textAlign = androidx.compose.ui.text.style.TextAlign.End, modifier = Modifier.weight(1.5f))
    }
}

@Preview(showBackground = true)
@Composable
private fun DataDeletionScreenPreview() {
    MaterialTheme {
        DataDeletionScreen(onBack = {})
    }
}
