package com.zoewave.probase.gotmind.features.settings.ui

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zoewave.probase.gotmind.features.memblox.MemBloxEvent
import com.zoewave.probase.gotmind.features.memblox.MemBloxState
import com.zoewave.probase.gotmind.features.settings.R
import com.zoewave.probase.gotmind.model.AppTheme
import com.zoewave.probase.gotmind.model.ColorPalette
import com.zoewave.probase.gotmind.model.ThemeSettings
import com.zoewave.probase.gotmind.model.MemBloxEngineType
import com.zoewave.probase.gotmind.model.MindWaveMode
import com.zoewave.probase.gotmind.model.InstrumentType
import com.zoewave.probase.gotmind.model.GameSettings
import java.util.Locale

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
    )
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun <T> SettingsDropdownItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    currentValue: T,
    options: List<T>,
    optionLabels: Map<T, String>,
    onSelected: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = Color.Gray)
                Spacer(modifier = Modifier.width(16.dp))
                Text(title, color = Color.White, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(12.dp))

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = optionLabels[currentValue] ?: currentValue.toString(),
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                        unfocusedTextColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(Color(0xFF1E1E1E))
                ) {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(optionLabels[option] ?: option.toString(), color = Color.White) },
                            onClick = {
                                onSelected(option)
                                expanded = false
                            },
                            modifier = Modifier.background(Color(0xFF1E1E1E))
                        )
                    }
                }
            }
        }
    }
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

@Composable
fun SettingsScreen(
    gameSettings: GameSettings = GameSettings(),
    themeSettings: ThemeSettings = ThemeSettings(),
    firebaseId: String = "",
    onMemBloxEvent: (MemBloxEvent) -> Unit = {},
    onSettingsEvent: (SettingsEvent) -> Unit = {},
    onBack: () -> Unit = {}
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
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                    contentDescription = null, 
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
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

        // --- Themes & Colors ---
        SettingsSectionTitle(stringResource(R.string.applications_gotmind_features_settings_theme_section))

        SettingsDropdownItem(
            icon = Icons.Default.BrightnessMedium,
            title = stringResource(R.string.applications_gotmind_features_settings_theme_app),
            currentValue = themeSettings.theme,
            options = AppTheme.entries,
            optionLabels = mapOf(
                AppTheme.SYSTEM to stringResource(R.string.applications_gotmind_features_settings_theme_system),
                AppTheme.LIGHT to stringResource(R.string.applications_gotmind_features_settings_theme_light),
                AppTheme.DARK to stringResource(R.string.applications_gotmind_features_settings_theme_dark)
            ),
            onSelected = { onSettingsEvent(SettingsEvent.SetTheme(it)) }
        )

        SettingsDropdownItem(
            icon = Icons.Default.Palette,
            title = stringResource(R.string.applications_gotmind_features_settings_palette),
            currentValue = themeSettings.palette,
            options = ColorPalette.entries,
            optionLabels = mapOf(
                ColorPalette.DEFAULT to stringResource(R.string.applications_gotmind_features_settings_palette_default),
                ColorPalette.CORAL to stringResource(R.string.applications_gotmind_features_settings_palette_coral),
                ColorPalette.FOREST to stringResource(R.string.applications_gotmind_features_settings_palette_forest),
                ColorPalette.OCEAN to stringResource(R.string.applications_gotmind_features_settings_palette_ocean),
                ColorPalette.MATERIAL_EXPRESSIVE to stringResource(R.string.applications_gotmind_features_settings_palette_expressive)
            ),
            onSelected = { onSettingsEvent(SettingsEvent.SetPalette(it)) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- MindWave Settings ---
        SettingsSectionTitle(stringResource(R.string.applications_gotmind_features_settings_mindwave_section))

        SettingsDropdownItem(
            icon = Icons.Default.Info,
            title = stringResource(R.string.applications_gotmind_features_settings_mindwave_version),
            currentValue = gameSettings.mindWaveMode,
            options = MindWaveMode.entries,
            optionLabels = mapOf(
                MindWaveMode.CLASSIC to stringResource(R.string.applications_gotmind_features_settings_mindwave_classic),
                MindWaveMode.SYMPHONY to stringResource(R.string.applications_gotmind_features_settings_mindwave_symphony),
                MindWaveMode.HARMONIC_ARC to stringResource(R.string.applications_gotmind_features_settings_mindwave_arc)
            ),
            onSelected = { onSettingsEvent(SettingsEvent.SetMindWaveMode(it)) }
        )

        SettingsDropdownItem(
            icon = Icons.Default.VolumeUp,
            title = stringResource(R.string.applications_gotmind_features_settings_mw_instrument),
            currentValue = gameSettings.instrumentType,
            options = InstrumentType.entries,
            optionLabels = mapOf(
                InstrumentType.CLEAN_SYNTH to stringResource(R.string.applications_gotmind_features_settings_mw_instrument_clean),
                InstrumentType.RETRO_8BIT to stringResource(R.string.applications_gotmind_features_settings_mw_instrument_retro),
                InstrumentType.ZEN_TRIANGLE to stringResource(R.string.applications_gotmind_features_settings_mw_instrument_zen)
            ),
            onSelected = { onSettingsEvent(SettingsEvent.SetInstrument(it)) }
        )

        SettingItem(
            icon = Icons.Default.Info,
            title = stringResource(R.string.applications_gotmind_features_settings_mw_song_master),
            subtitle = stringResource(R.string.applications_gotmind_features_settings_mw_song_master_desc),
            checked = gameSettings.songMasterEnabled,
            onCheckedChange = { onSettingsEvent(SettingsEvent.SetSongMaster(it)) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- MemBlox Settings ---
        SettingsSectionTitle("MemBlox Settings")

        SettingItem(
            icon = Icons.Default.Speed,
            title = stringResource(com.zoewave.probase.gotmind.features.memblox.R.string.applications_gotmind_features_memblox_engine_mode),
            subtitle = stringResource(com.zoewave.probase.gotmind.features.memblox.R.string.applications_gotmind_features_memblox_engine_mode_desc),
            checked = gameSettings.engineType == MemBloxEngineType.FALLING,
            onCheckedChange = { isFalling: Boolean ->
                onMemBloxEvent(MemBloxEvent.SetEngineType(if (isFalling) MemBloxEngineType.FALLING else MemBloxEngineType.STATIC))
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Dynamic Speed
        val speedLabel = stringResource(com.zoewave.probase.gotmind.features.memblox.R.string.applications_gotmind_features_memblox_speed)
        val speedValue = String.format(Locale.getDefault(), "%.1f", gameSettings.gameSpeed)
        Text(
            text = "$speedLabel: ${speedValue}x",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Slider(
            value = gameSettings.gameSpeed,
            onValueChange = { onMemBloxEvent(MemBloxEvent.UpdateSpeed(it)) },
            valueRange = 0.5f..2.0f,
            steps = 15,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary, 
                activeTrackColor = MaterialTheme.colorScheme.primary
            )
        )

        if (gameSettings.engineType == MemBloxEngineType.FALLING) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(com.zoewave.probase.gotmind.features.memblox.R.string.applications_gotmind_features_memblox_drop_height, gameSettings.dropHeight),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Slider(
                value = gameSettings.dropHeight.toFloat(),
                onValueChange = { onMemBloxEvent(MemBloxEvent.UpdateDropHeight(it.toInt())) },
                valueRange = 1f..10f,
                steps = 9,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary, 
                    activeTrackColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))
            val dropDurationSec = gameSettings.dropDurationMillis / 1000f
            Text(
                text = stringResource(com.zoewave.probase.gotmind.features.memblox.R.string.applications_gotmind_features_memblox_drop_duration, dropDurationSec),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Slider(
                value = gameSettings.dropDurationMillis.toFloat(),
                onValueChange = { onMemBloxEvent(MemBloxEvent.UpdateDropDuration(it.toInt())) },
                valueRange = 1000f..10000f,
                steps = 18,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary, 
                    activeTrackColor = MaterialTheme.colorScheme.primary
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Game Settings ---
        SettingsSectionTitle(stringResource(R.string.applications_gotmind_features_settings_game_section))
        
        SettingItem(
            icon = Icons.Default.Vibration,
            title = stringResource(R.string.applications_gotmind_features_settings_haptic_title),
            subtitle = stringResource(R.string.applications_gotmind_features_settings_haptic_subtitle),
            checked = gameSettings.hapticsEnabled,
            onCheckedChange = { onMemBloxEvent(MemBloxEvent.SetHapticsEnabled(it)) }
        )
        
        SettingItem(
            icon = Icons.Default.VolumeUp,
            title = stringResource(R.string.applications_gotmind_features_settings_sound_title),
            subtitle = stringResource(R.string.applications_gotmind_features_settings_sound_subtitle),
            checked = gameSettings.soundEnabled,
            onCheckedChange = { onMemBloxEvent(MemBloxEvent.SetSoundEnabled(it)) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- App Settings ---
        SettingsSectionTitle(stringResource(R.string.applications_gotmind_features_settings_app_section))
        
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
                
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(modifier = Modifier.fillMaxWidth(), color = Color.White.copy(alpha = 0.1f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.applications_gotmind_features_settings_about_finding_id),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                
                if (firebaseId.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.applications_gotmind_features_settings_about_id_label, firebaseId),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
