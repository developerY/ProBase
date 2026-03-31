package com.zoewave.probase.photodo.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.zoewave.probase.applications.photodo.db.repo.AppSettingsRepository
import com.zoewave.probase.photodo.mobile.core.ui.theme.PhotoDoTheme
import com.zoewave.probase.photodo.mobile.ui.components.PhotoDoMainScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // 1. Inject the repo directly into the Activity to get the root state
    @Inject
    lateinit var appSettingsRepository: AppSettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Tags all future events from this user's phone
        val firebaseAnalytics = Firebase.analytics
        firebaseAnalytics.setUserProperty("device_platform", "mobile")
        firebaseAnalytics.setUserProperty("app_flavor", "photodo")
        firebaseAnalytics.setUserProperty("app_type", "probase")
        firebaseAnalytics.setUserProperty("app_id", "com.zoewave.probase.photodo.mobile")
        firebaseAnalytics.setUserProperty("app_version", BuildConfig.VERSION_NAME)
        firebaseAnalytics.setUserProperty("build_type", BuildConfig.BUILD_TYPE)

        enableEdgeToEdge()
        setContent {

            // 2. Collect the current theme state
            val themePreference by appSettingsRepository.themePreferenceFlow.collectAsState(initial = "SYSTEM")
            val palettePreference by appSettingsRepository.palettePreferenceFlow.collectAsState(initial = "DEFAULT") // NEW

            // 3. Determine true/false for Dark Mode
            val isDarkTheme = when (themePreference) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemInDarkTheme() // "SYSTEM" fallback
            }

            // 4. Pass it to your beautiful custom theme!
            PhotoDoTheme(
                darkTheme = isDarkTheme,
                palette = palettePreference
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PhotoDoMainScreen()
                }
            }
        }
    }
}