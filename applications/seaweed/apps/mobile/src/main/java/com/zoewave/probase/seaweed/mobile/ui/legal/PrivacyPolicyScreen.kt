package com.zoewave.probase.seaweed.mobile.ui.legal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
fun PrivacyPolicyScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.applications_seaweed_mobile_legal_privacy_policy), fontWeight = FontWeight.Black) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(stringResource(R.string.applications_seaweed_mobile_legal_effective_date), style = MaterialTheme.typography.labelMedium)
            
            PolicySection(
                title = stringResource(R.string.applications_seaweed_mobile_legal_zero_footprint_title),
                content = stringResource(R.string.applications_seaweed_mobile_legal_zero_footprint_desc)
            )

            PolicySection(
                title = stringResource(R.string.applications_seaweed_mobile_legal_gen_ai_title),
                content = stringResource(R.string.applications_seaweed_mobile_legal_gen_ai_desc)
            )

            PolicySection(
                title = stringResource(R.string.applications_seaweed_mobile_legal_permissions_title),
                content = stringResource(R.string.applications_seaweed_mobile_legal_permissions_desc)
            )

            PolicySection(
                title = stringResource(R.string.applications_seaweed_mobile_legal_third_party_title),
                content = stringResource(R.string.applications_seaweed_mobile_legal_third_party_desc)
            )

            PolicySection(
                title = stringResource(R.string.applications_seaweed_mobile_legal_sovereignty_title),
                content = stringResource(R.string.applications_seaweed_mobile_legal_sovereignty_desc)
            )

            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                stringResource(R.string.applications_seaweed_mobile_legal_contact),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PolicySection(title: String, content: String) {
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

@Preview(showBackground = true)
@Composable
private fun PrivacyPolicyScreenPreview() {
    MaterialTheme {
        PrivacyPolicyScreen(onBack = {})
    }
}
