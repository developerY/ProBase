package com.zoewave.probase.photodo.mobile.features.settings.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zoewave.probase.photodo.mobile.features.settings.R

@Composable
fun AboutSettingsCard(
    expanded: Boolean,
    onExpandToggle: () -> Unit,
    appVersion: String,
    firebaseDeviceId: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    Card(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .clickable { onExpandToggle() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = stringResource(R.string.applications_photodo_apps_mobile_features_settings_about_title),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = stringResource(if (expanded) R.string.applications_photodo_apps_mobile_features_settings_collapse_content_description else R.string.applications_photodo_apps_mobile_features_settings_expand_content_description)
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(16.dp)) {
                    HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))

                    // Version Information
                    Text(
                        text = stringResource(R.string.applications_photodo_apps_mobile_features_settings_about_version_label),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = appVersion,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Firebase Device ID
                    Text(
                        text = stringResource(R.string.applications_photodo_apps_mobile_features_settings_about_firebase_id_label),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = firebaseDeviceId,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = stringResource(R.string.applications_photodo_apps_mobile_features_settings_about_firebase_id_description),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    TextButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Firebase Device ID", firebaseDeviceId)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, R.string.applications_photodo_apps_mobile_features_settings_about_id_copied, Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text(stringResource(R.string.applications_photodo_apps_mobile_features_settings_about_copy_id))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Legal Section
                    Text(
                        text = stringResource(R.string.applications_photodo_apps_mobile_features_settings_about_legal_title),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = stringResource(R.string.applications_photodo_apps_mobile_features_settings_about_privacy_policy),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable { uriHandler.openUri("https://github.com/your-org/probase/blob/main/applications/photodo/docs/PrivacyPolicy.md") }
                            .padding(vertical = 8.dp)
                            .fillMaxWidth()
                    )
                    
                    Text(
                        text = stringResource(R.string.applications_photodo_apps_mobile_features_settings_about_eula),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable { uriHandler.openUri("https://github.com/your-org/probase/blob/main/applications/photodo/docs/EULA.md") }
                            .padding(vertical = 8.dp)
                            .fillMaxWidth()
                    )
                }
            }
        }
    }
}
