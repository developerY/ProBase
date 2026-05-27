package com.zoewave.probase.seaweed.mobile.glass

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.xr.glimmer.Button
import androidx.xr.glimmer.Card
import androidx.xr.glimmer.GlimmerTheme
import androidx.xr.glimmer.Icon
import androidx.xr.glimmer.Text
import androidx.xr.glimmer.TitleChip
import androidx.xr.glimmer.stack.VerticalStack
import com.zoewave.probase.core.util.CurrencyUtils
import com.zoewave.probase.seaweed.model.FinancialProfile

@Composable
fun SeaweedGlassApp(
    uiState: SeaweedGlassUiState,
    onTalkToGemini: () -> Unit,
    onCaptureImage: () -> Unit,
    onClose: () -> Unit
) {
    val profile = uiState.profile

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 1. HEADER: BRANDING & SYSTEM STATUS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SEAWEED",
                style = GlimmerTheme.typography.titleMedium,
                color = GlimmerTheme.colors.primary
            )
            
            // Minimal Status
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.AccountBalanceWallet,
                    contentDescription = null,
                    tint = GlimmerTheme.colors.outline,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "LIVE",
                    style = GlimmerTheme.typography.caption,
                    color = GlimmerTheme.colors.outline
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // 2. MAIN CONTENT: IMMERSIVE STACK
        VerticalStack(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // ITEM 1: FLEXIBLE BALANCE (Hero)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().itemDecoration(GlimmerTheme.shapes.medium),
                    onClick = {} // Focusable
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "FLEXIBLE MONEY",
                            style = GlimmerTheme.typography.caption,
                            color = GlimmerTheme.colors.outline
                        )
                        val balance = profile?.let { CurrencyUtils.formatCents(it.flexibleMoneyRemainingCents) } ?: "---"
                        Text(
                            text = balance,
                            style = GlimmerTheme.typography.titleLarge,
                            color = GlimmerTheme.colors.secondary
                        )
                        
                        val progress = profile?.let { (it.monthProgress * 100).toInt() } ?: 0
                        Text(
                            text = "Month Progress: $progress%",
                            style = GlimmerTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // ITEM 2: AI ANALYSIS / AFFORDABILITY
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().itemDecoration(GlimmerTheme.shapes.medium),
                    onClick = onCaptureImage,
                    title = {
                        Text(if (uiState.isAnalyzing) "Analyzing..." else "Can I Afford This?")
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.CameraAlt,
                            contentDescription = null,
                            tint = if (uiState.isAnalyzing) GlimmerTheme.colors.secondary else Color.White
                        )
                    }
                ) {
                    if (uiState.lastAnalysisResult != null) {
                        Text(
                            text = uiState.lastAnalysisResult,
                            style = GlimmerTheme.typography.bodySmall,
                            color = Color.White,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    } else {
                        Text(
                            text = "Point your glasses at an item to check your budget impact.",
                            style = GlimmerTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            // ITEM 3: GEMINI LIVE
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().itemDecoration(GlimmerTheme.shapes.medium),
                    onClick = onTalkToGemini,
                    title = { Text("Talk to Assistant") },
                    leadingIcon = {
                        Icon(
                            Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = GlimmerTheme.colors.primary
                        )
                    }
                ) {
                    Text(
                        text = "Start a voice conversation about your finances.",
                        style = GlimmerTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // 3. EXIT BUTTON
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Button(onClick = onClose) {
                Text("EXIT")
            }
        }
    }
}
