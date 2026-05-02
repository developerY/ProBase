package com.zoewave.probase.gotmind.features.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zoewave.probase.gotmind.features.settings.R
import com.zoewave.probase.gotmind.features.memblox.MemBloxEngineType

@Composable
fun SettingsScreen(
    engineType: MemBloxEngineType = MemBloxEngineType.STATIC,
    onEngineTypeChange: (MemBloxEngineType) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F))
            .statusBarsPadding()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Settings, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.applications_gotmind_features_settings_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- Game Settings ---
        SettingsSectionTitle(stringResource(R.string.applications_gotmind_features_settings_game_section))

        SettingItem(
            icon = Icons.Default.Speed,
            title = stringResource(com.zoewave.probase.gotmind.features.memblox.R.string.applications_gotmind_features_memblox_engine_mode),
            subtitle = stringResource(com.zoewave.probase.gotmind.features.memblox.R.string.applications_gotmind_features_memblox_engine_mode_desc),
            checked = engineType == MemBloxEngineType.FALLING,
            onCheckedChange = { isFalling ->
                onEngineTypeChange(if (isFalling) MemBloxEngineType.FALLING else MemBloxEngineType.STATIC)
            }
        )
        
        SettingItem(
            icon = Icons.Default.Vibration,
            title = stringResource(R.string.applications_gotmind_features_settings_haptic_title),
            subtitle = stringResource(R.string.applications_gotmind_features_settings_haptic_subtitle),
            checked = true,
            onCheckedChange = {}
        )
        
        SettingItem(
            icon = Icons.Default.VolumeUp,
            title = stringResource(R.string.applications_gotmind_features_settings_sound_title),
            subtitle = stringResource(R.string.applications_gotmind_features_settings_sound_subtitle),
            checked = true,
            onCheckedChange = {}
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- App Settings ---
        SettingsSectionTitle(stringResource(R.string.applications_gotmind_features_settings_app_section))
        
        SettingItem(
            icon = Icons.Default.Settings,
            title = stringResource(R.string.applications_gotmind_features_settings_dark_mode_title),
            subtitle = stringResource(R.string.applications_gotmind_features_settings_dark_mode_subtitle),
            checked = true,
            onCheckedChange = {}
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- About ---
        SettingsSectionTitle(stringResource(R.string.applications_gotmind_features_settings_about_section))
        
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF1E1E1E))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color.Gray)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.applications_gotmind_features_settings_about_version), 
                        color = Color.White, 
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.applications_gotmind_features_settings_about_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 16.dp)
    )
}

@Composable
fun SettingItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = Color.Gray)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}
