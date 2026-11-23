package com.zoewave.ashbike.mobile.settings.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.ashbike.model.bike.LocationEnergyLevel
import com.zoewave.probase.ashbike.database.ProfileData
import com.zoewave.probase.ashbike.database.repository.AppSettingsRepository
import com.zoewave.probase.ashbike.database.repository.UserProfileRepository
import com.zoewave.probase.core.util.combine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appRepo: AppSettingsRepository,
    private val profileRepo: UserProfileRepository
) : ViewModel() {

    // MINIMAL CHANGE 1 of 2: Add a temporary holder for the UI's selection.
    private val _locallySelectedEnergyLevel = MutableStateFlow<LocationEnergyLevel?>(null)

    // ✅ NEW: Temporary holder for which section is expanded (e.g., "app_prefs")
    private val _expandedSection = MutableStateFlow<String?>(null)

    val theme: StateFlow<String> = appRepo.themeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = "System"
        )

    private val staticOptions = mapOf(
        "Theme" to listOf("Light", "Dark", "System Default"),
        "Language" to listOf("English", "Spanish", "French"),
        "Notifications" to listOf("Enabled", "Disabled"),
        "Units" to listOf("Imperial (English)", "Metric (SI)"),
        "GPS Accuracy" to listOf("Low Power", "Balanced", "High Accuracy")
    )

    private val settingsSelections = combine(
        appRepo.themeFlow,
        appRepo.languageFlow,
        appRepo.notificationsFlow,
        appRepo.unitsFlow,
        appRepo.gpsAccuracyFlow,
        appRepo.longRideEnabledFlow // Added for isLongRideEnabled
    ) { theme, lang, notif, units, gpsAccuracyEnum, isLongRideEnabled -> // Added isLongRideEnabled
        mapOf(
            "Theme" to theme,
            "Language" to lang,
            "Notifications" to notif,
            "Units" to units,
            "GPS Accuracy" to gpsAccuracyEnum.name,
            AppPreferenceKeys.KEY_LONG_RIDE_ENABLED to isLongRideEnabled.toString() // Added for isLongRideEnabled
        )
    }

    private val profileData = combine(
        profileRepo.nameFlow,
        profileRepo.heightFlow,
        profileRepo.weightFlow
    ) { name, heightStr, weightStr ->
        ProfileData(name = name, heightCm = heightStr, weightKg = weightStr)
    }

    /* Expose the showGpsCountdownFlow
    val showGpsCountdown: StateFlow<Boolean> = profileRepo.showGpsCountdownFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = true
        )*/

    private val telemetryStateFlow = combine(
        appRepo.crashlyticsEnabledFlow,
        appRepo.analyticsEnabledFlow
    ) { crash, analytics ->
        Pair(crash, analytics)
    }

    val uiState: StateFlow<SettingsUiState> =
        combine(
            _locallySelectedEnergyLevel,
            settingsSelections,
            profileData,
            appRepo.gpsAccuracyFlow,
            telemetryStateFlow,
            _expandedSection
        ) { localOverride, selections, profile, savedEnergyLevel, telemetry, expandedSectionId ->

            val energyLevel = localOverride ?: savedEnergyLevel
            val isLongRideEnabled = selections[AppPreferenceKeys.KEY_LONG_RIDE_ENABLED]?.toBoolean() ?: false

            SettingsUiState.Success(
                options = staticOptions,
                selections = selections,
                profile = profile,
                isProfileIncomplete = false,
                currentEnergyLevel = energyLevel,
                isLongRideEnabled = isLongRideEnabled,
                isCrashlyticsEnabled = telemetry.first,
                isAnalyticsEnabled = telemetry.second,
                expandedSectionId = expandedSectionId
            )
        }
            .map { successState -> successState as SettingsUiState }
            .catch { e ->
                Log.e("ViewModelCombine", "Error in uiState combine: ${e.message}", e)
                emit(SettingsUiState.Error("Failed to combine UI state: ${e.message}"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = SettingsUiState.Loading
            )

    fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.LoadSettings -> {
                // This is a no-op as the flows are hot and actively combined.
            }

            is SettingsEvent.UpdateSetting -> {
                viewModelScope.launch {
                    when (event.key) {
                        "Theme" -> appRepo.setTheme(event.value)
                        "Language" -> appRepo.setLanguage(event.value)
                        "Notifications" -> appRepo.setNotifications(event.value)
                        "Units" -> appRepo.setUnits(event.value)
                        "GPS Accuracy" -> appRepo.setGpsAccuracy(event.value)
                        else -> Log.w("SettingsViewModel", "Unhandled setting key: ${event.key}")
                    }
                }
            }

            is SettingsEvent.SaveProfile -> {
                viewModelScope.launch {
                    Log.d(
                        "SettingsViewModel",
                        "Saving profile: Name=${event.profile.name}, H=${event.profile.heightCm}, W=${event.profile.weightKg}"
                    )
                    try {
                        profileRepo.saveProfile(event.profile)
                        Log.d(
                            "SettingsViewModel",
                            "Profile save initiated for ${event.profile.name}"
                        )
                    } catch (e: Exception) {
                        Log.e(
                            "SettingsViewModel",
                            "Failed to save profile for ${event.profile.name}",
                            e
                        )
                    }
                }
            }

            is SettingsEvent.UpdateEnergyLevel -> {
                // MINIMAL CHANGE 2 of 2: Update the temporary holder instantly for the UI.
                _locallySelectedEnergyLevel.value = event.level
                viewModelScope.launch {
                    Log.d("SettingsViewModel", "Updating Energy Level to: ${event.level.name}")
                    try {
                        appRepo.setGpsAccuracy(event.level.name)
                        Log.d("SettingsViewModel", "Successfully called repo to set energy level.")
                    } catch (e: Exception) {
                        Log.e("SettingsViewModel", "Failed to set energy level.", e)
                    }
                }
            }

            is SettingsEvent.UpdateLongRideEnabled -> { // Added handler for UpdateLongRideEnabled
                viewModelScope.launch {
                    Log.d("SettingsViewModel", "Updating Long Ride Enabled to: ${event.enabled}")
                    try {
                        appRepo.setLongRideEnabled(event.enabled)
                    } catch (e: Exception) {
                        Log.e("SettingsViewModel", "Failed to set long ride enabled.", e)
                    }
                }
            }

            is SettingsEvent.UpdateCrashlyticsEnabled -> {
                viewModelScope.launch {
                    try {
                        appRepo.setCrashlyticsEnabled(event.enabled)
                    } catch (e: Exception) {
                        Log.e("SettingsViewModel", "Failed to set Crashlytics enabled.", e)
                    }
                }
            }

            is SettingsEvent.UpdateAnalyticsEnabled -> {
                viewModelScope.launch {
                    try {
                        appRepo.setAnalyticsEnabled(event.enabled)
                    } catch (e: Exception) {
                        Log.e("SettingsViewModel", "Failed to set Analytics enabled.", e)
                    }
                }
            }

            // ✅ 4. Handle the new event to set/clear the expanded section
            is SettingsEvent.OnSetExpandedSection -> {
                Log.d("DEBUG_NAV", "ViewModel received OnSetExpandedSection: ${event.sectionId}")
                _expandedSection.value = event.sectionId
            }


            /* Handle the new event
            is SettingsEvent.OnShowGpsCountdownChanged -> {
                viewModelScope.launch {
                    profileRepo.setShowGpsCountdown(event.show)
                }
            }*/
        }
    }
}
