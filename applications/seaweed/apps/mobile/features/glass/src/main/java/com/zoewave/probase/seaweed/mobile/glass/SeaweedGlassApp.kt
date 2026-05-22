package com.zoewave.probase.seaweed.mobile.glass

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.xr.glimmer.Button
import androidx.xr.glimmer.Card
import androidx.xr.glimmer.GlimmerTheme
import androidx.xr.glimmer.Text
import androidx.xr.glimmer.TitleChip
import com.zoewave.probase.seaweed.model.FinancialProfile
import java.text.NumberFormat
import java.util.Locale

@Composable
fun SeaweedGlassApp(
    profile: FinancialProfile,
    analysisResult: String? = null,
    onTalkToGemini: () -> Unit = {},
    onCaptureImage: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TitleChip { Text("Seaweed Balance") }
        
        Card(
            onClick = { /* Could navigate to details */ }
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val formattedBalance = NumberFormat.getCurrencyInstance(Locale.US)
                    .format(profile.flexibleMoneyRemainingCents / 100.0)
                
                Text(
                    text = formattedBalance,
                    style = GlimmerTheme.typography.titleLarge,
                    color = GlimmerTheme.colors.primary
                )
                
                Text(
                    text = "Flexible Money Remaining",
                    style = GlimmerTheme.typography.bodyMedium,
                    color = GlimmerTheme.colors.outline
                )
            }
        }
        
        // Show month progress
        Text(
            text = "Month Progress: ${(profile.monthProgress * 100).toInt()}%",
            style = GlimmerTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 16.dp)
        )

        analysisResult?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Card {
                Text(
                    text = it,
                    style = GlimmerTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = onCaptureImage
            ) {
                Text("Analyze View")
            }

            Button(
                onClick = onTalkToGemini
            ) {
                Text("Gemini Live")
            }
        }
    }
}
