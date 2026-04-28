package com.zoewave.probase.seaweed.mobile.ui.legal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
fun PrivacyPolicyScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy Policy", fontWeight = FontWeight.Black) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Effective Date: April 21, 2026", style = MaterialTheme.typography.labelMedium)
            
            PolicySection(
                title = "1. Zero-Footprint Mandate",
                content = "Seaweed stores all data—transactions, budget envelopes, and images—exclusively in a secure local database on your device. We do not utilize central servers and have no access to your information."
            )

            PolicySection(
                title = "2. Generative AI (BYOK)",
                content = "AI extraction is optional. If you provide a Gemini API Key, image data is transmitted directly to Google for processing. This data is handled ephemerally and is not retained or viewed by ZoeWave LLC."
            )

            PolicySection(
                title = "3. Device Permissions",
                content = "Camera: Solely used to capture receipts for extraction.\nLocation: Optionally used to tag transactions spatially.\nStorage: Used for local persistence of your data."
            )

            PolicySection(
                title = "4. Third-Party Services",
                content = "We use standard SDKs (Firebase/Google Play) for stability and anonymized analytics. Payment data for Stripe or Google Pay is handled directly by those providers under their own policies."
            )

            PolicySection(
                title = "5. Data Sovereignty",
                content = "You maintain absolute control. Deleting the application permanently removes all data. We recommend using device-level biometrics to secure your local financial enclave."
            )

            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                "Questions? Contact Developer@ZoeWave.com",
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
