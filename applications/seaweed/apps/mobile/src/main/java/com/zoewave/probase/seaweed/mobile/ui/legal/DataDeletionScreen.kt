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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.seaweed.mobile.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataDeletionScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.applications_seaweed_mobile_legal_data_deletion), fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.applications_seaweed_mobile_legal_back))
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
                text = stringResource(R.string.applications_seaweed_mobile_legal_deletion_intro),
                style = MaterialTheme.typography.bodyLarge
            )

            DeletionSection(
                title = stringResource(R.string.applications_seaweed_mobile_legal_personal_content_title),
                content = stringResource(R.string.applications_seaweed_mobile_legal_personal_content_desc)
            )

            DeletionSection(
                title = stringResource(R.string.applications_seaweed_mobile_legal_analytics_deletion_title),
                content = stringResource(R.string.applications_seaweed_mobile_legal_analytics_deletion_desc)
            )

            RetentionTable()

            Text(
                stringResource(R.string.applications_seaweed_mobile_legal_contact_alt),
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
            Text(stringResource(R.string.applications_seaweed_mobile_legal_retention_policy), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            
            RetentionRow(stringResource(R.string.applications_seaweed_mobile_legal_financial_data), stringResource(R.string.applications_seaweed_mobile_legal_deleted_on_uninstall))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            RetentionRow(stringResource(R.string.applications_seaweed_mobile_legal_receipt_images), stringResource(R.string.applications_seaweed_mobile_legal_deleted_on_uninstall))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            RetentionRow(stringResource(R.string.applications_seaweed_mobile_legal_crash_logs), stringResource(R.string.applications_seaweed_mobile_legal_scrubbed_90_days))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            RetentionRow(stringResource(R.string.applications_seaweed_mobile_legal_analytics_ids), stringResource(R.string.applications_seaweed_mobile_legal_kept_14_months))
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
