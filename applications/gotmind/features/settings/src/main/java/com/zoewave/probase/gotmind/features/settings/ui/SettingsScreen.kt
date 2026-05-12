package com.zoewave.probase.gotmind.features.settings.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
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
import androidx.compose.ui.unit.sp
import com.zoewave.probase.gotmind.features.memblox.MemBloxEvent
import com.zoewave.probase.gotmind.features.memblox.MemBloxState
import com.zoewave.probase.gotmind.features.settings.R
import com.zoewave.probase.gotmind.model.AppTheme
import com.zoewave.probase.gotmind.model.ColorPalette
import com.zoewave.probase.gotmind.model.ThemeSettings
import com.zoewave.probase.gotmind.model.MemBloxEngineType
import com.zoewave.probase.gotmind.model.MindWaveMode
import com.zoewave.probase.gotmind.model.NodeShape
import com.zoewave.probase.gotmind.model.InstrumentType
import com.zoewave.probase.gotmind.model.GameSettings
import java.util.Locale

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 24.dp, bottom = 8.dp, start = 4.dp)
    )
}

@Composable
fun ExpandableSettingsCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(title, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color.Gray
                )
            }
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 20.dp)) {
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(bottom = 16.dp))
                    content()
                }
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun <T> SettingsDropdownField(
    label: String,
    currentValue: T,
    options: List<T>,
    optionLabels: Map<T, String>,
    onSelected: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))
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
                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp),
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

@Composable
fun SettingsSliderField(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    onValueChange: (Float) -> Unit,
    valueLabel: String
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            Text(valueLabel, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = Color.White.copy(alpha = 0.1f)
            )
        )
    }
}

@Composable
fun SettingsSwitchField(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
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
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // --- Header ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.applications_gotmind_features_settings_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }

        // --- Visual Experience ---
        SettingsSectionTitle(stringResource(R.string.applications_gotmind_features_settings_theme_section))
        
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF1A1A1A))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                SettingsDropdownField(
                    label = stringResource(R.string.applications_gotmind_features_settings_theme_app),
                    currentValue = themeSettings.theme,
                    options = AppTheme.entries,
                    optionLabels = mapOf(
                        AppTheme.SYSTEM to stringResource(R.string.applications_gotmind_features_settings_theme_system),
                        AppTheme.LIGHT to stringResource(R.string.applications_gotmind_features_settings_theme_light),
                        AppTheme.DARK to stringResource(R.string.applications_gotmind_features_settings_theme_dark)
                    ),
                    onSelected = { onSettingsEvent(SettingsEvent.SetTheme(it)) }
                )

                SettingsDropdownField(
                    label = stringResource(R.string.applications_gotmind_features_settings_palette),
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
            }
        }

        // --- Global Game Toggles ---
        SettingsSectionTitle(stringResource(R.string.applications_gotmind_features_settings_section_system))
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF1A1A1A))
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                SettingsSwitchField(
                    title = stringResource(R.string.applications_gotmind_features_settings_haptic_title),
                    subtitle = stringResource(R.string.applications_gotmind_features_settings_haptic_subtitle),
                    checked = gameSettings.hapticsEnabled,
                    onCheckedChange = { onMemBloxEvent(MemBloxEvent.SetHapticsEnabled(it)) }
                )
                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                SettingsSwitchField(
                    title = stringResource(R.string.applications_gotmind_features_settings_sound_title),
                    subtitle = stringResource(R.string.applications_gotmind_features_settings_sound_subtitle),
                    checked = gameSettings.soundEnabled,
                    onCheckedChange = { onMemBloxEvent(MemBloxEvent.SetSoundEnabled(it)) }
                )
            }
        }

        // --- Game Specific Modules ---
        SettingsSectionTitle(stringResource(R.string.applications_gotmind_features_settings_section_arcade))

        ExpandableSettingsCard(
            title = stringResource(R.string.applications_gotmind_features_settings_module_soundmind),
            icon = Icons.Default.MusicNote
        ) {
            SettingsDropdownField(
                label = stringResource(R.string.applications_gotmind_features_settings_mindwave_version),
                currentValue = gameSettings.mindWaveMode,
                options = MindWaveMode.entries,
                optionLabels = mapOf(
                    MindWaveMode.CLASSIC to stringResource(R.string.applications_gotmind_features_settings_mindwave_classic),
                    MindWaveMode.SYMPHONY to stringResource(R.string.applications_gotmind_features_settings_mindwave_symphony),
                    MindWaveMode.HARMONIC_ARC to stringResource(R.string.applications_gotmind_features_settings_mindwave_arc),
                    MindWaveMode.HARMONIC_RING to stringResource(R.string.applications_gotmind_features_settings_mindwave_ring)
                ),
                onSelected = { onSettingsEvent(SettingsEvent.SetMindWaveMode(it)) }
            )

            SettingsDropdownField(
                label = stringResource(R.string.applications_gotmind_features_settings_mw_node_shape),
                currentValue = gameSettings.mindWaveNodeShape,
                options = NodeShape.entries,
                optionLabels = mapOf(
                    NodeShape.CIRCLE to stringResource(R.string.applications_gotmind_features_settings_mw_node_shape_circle),
                    NodeShape.PIANO_KEY to stringResource(R.string.applications_gotmind_features_settings_mw_node_shape_piano)
                ),
                onSelected = { onSettingsEvent(SettingsEvent.SetMindWaveNodeShape(it)) }
            )

            SettingsDropdownField(
                label = stringResource(R.string.applications_gotmind_features_settings_mw_instrument),
                currentValue = gameSettings.instrumentType,
                options = InstrumentType.entries,
                optionLabels = mapOf(
                    InstrumentType.CLEAN_SYNTH to stringResource(R.string.applications_gotmind_features_settings_mw_instrument_clean),
                    InstrumentType.RETRO_8BIT to stringResource(R.string.applications_gotmind_features_settings_mw_instrument_retro),
                    InstrumentType.ZEN_TRIANGLE to stringResource(R.string.applications_gotmind_features_settings_mw_instrument_zen)
                ),
                onSelected = { onSettingsEvent(SettingsEvent.SetInstrument(it)) }
            )

            SettingsSwitchField(
                title = stringResource(R.string.applications_gotmind_features_settings_mw_song_master),
                subtitle = stringResource(R.string.applications_gotmind_features_settings_mw_song_master_desc),
                checked = gameSettings.songMasterEnabled,
                onCheckedChange = { onSettingsEvent(SettingsEvent.SetSongMaster(it)) }
            )
        }

        ExpandableSettingsCard(
            title = stringResource(R.string.applications_gotmind_features_settings_module_memblox),
            icon = Icons.Default.Speed
        ) {
            SettingsSwitchField(
                title = stringResource(com.zoewave.probase.gotmind.features.memblox.R.string.applications_gotmind_features_memblox_engine_mode),
                subtitle = stringResource(com.zoewave.probase.gotmind.features.memblox.R.string.applications_gotmind_features_memblox_engine_mode_desc),
                checked = gameSettings.engineType == MemBloxEngineType.FALLING,
                onCheckedChange = { isFalling: Boolean ->
                    onMemBloxEvent(MemBloxEvent.SetEngineType(if (isFalling) MemBloxEngineType.FALLING else MemBloxEngineType.STATIC))
                }
            )

            SettingsSliderField(
                label = stringResource(com.zoewave.probase.gotmind.features.memblox.R.string.applications_gotmind_features_memblox_speed),
                value = gameSettings.gameSpeed,
                valueRange = 0.5f..2.0f,
                steps = 15,
                onValueChange = { onMemBloxEvent(MemBloxEvent.UpdateSpeed(it)) },
                valueLabel = String.format(Locale.getDefault(), "%.1f", gameSettings.gameSpeed) + "x"
            )

            if (gameSettings.engineType == MemBloxEngineType.FALLING) {
                SettingsSliderField(
                    label = stringResource(com.zoewave.probase.gotmind.features.memblox.R.string.applications_gotmind_features_memblox_drop_height, gameSettings.dropHeight),
                    value = gameSettings.dropHeight.toFloat(),
                    valueRange = 1f..10f,
                    steps = 9,
                    onValueChange = { onMemBloxEvent(MemBloxEvent.UpdateDropHeight(it.toInt())) },
                    valueLabel = "${gameSettings.dropHeight}"
                )

                SettingsSliderField(
                    label = stringResource(com.zoewave.probase.gotmind.features.memblox.R.string.applications_gotmind_features_memblox_drop_duration, gameSettings.dropDurationMillis / 1000f),
                    value = gameSettings.dropDurationMillis.toFloat(),
                    valueRange = 1000f..10000f,
                    steps = 18,
                    onValueChange = { onMemBloxEvent(MemBloxEvent.UpdateDropDuration(it.toInt())) },
                    valueLabel = String.format(Locale.getDefault(), "%.1f", gameSettings.dropDurationMillis / 1000f) + "s"
                )
            }
        }

        // --- Support & Legal ---
        SettingsSectionTitle(stringResource(R.string.applications_gotmind_features_settings_section_support))
        
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF1A1A1A))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
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
                HorizontalDivider(modifier = Modifier.fillMaxWidth(), color = Color.White.copy(alpha = 0.05f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.applications_gotmind_features_settings_about_finding_id),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    lineHeight = 16.sp
                )
                
                if (firebaseId.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = firebaseId,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
    }
}
