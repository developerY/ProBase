package com.zoewave.probase.photodo.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.zoewave.probase.photodo.mobile.core.ui.theme.LocalPaneContrast
import com.zoewave.probase.photodo.mobile.core.ui.theme.PhotoDoTheme
import com.zoewave.probase.photodo.mobile.ui.components.PhotoDoMainScreen
import com.zoewave.probase.photodo.mobile.ui.components.PhotoDoMainViewModel
import com.zoewave.probase.photodo.mobile.ui.components.PhotoDoMainUiState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
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
            val viewModel: PhotoDoMainViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            // 3. Determine true/false for Dark Mode
            val isDarkTheme = when (uiState.theme) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemInDarkTheme() // "SYSTEM" fallback
            }

            // 4. Pass it to your beautiful custom theme!
            PhotoDoTheme(
                darkTheme = isDarkTheme,
                palette = uiState.palette
            ) {
                val windowSizeClass = calculateWindowSizeClass(this)
                CompositionLocalProvider(LocalPaneContrast provides uiState.paneContrast) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        PhotoDoMainScreen(windowSizeClass = windowSizeClass, viewModel = viewModel)
                    }
                }
            }
        }
    }
}
