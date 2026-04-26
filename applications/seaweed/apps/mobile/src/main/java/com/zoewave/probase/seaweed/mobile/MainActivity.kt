package com.zoewave.probase.seaweed.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.zoewave.probase.features.payment.stripe.ui.StripePaymentProvider
import com.zoewave.probase.seaweed.data.CategoryRepository
import com.zoewave.probase.seaweed.data.FinancialRepository
import com.zoewave.probase.seaweed.data.UserSettingsRepository
import com.zoewave.probase.seaweed.mobile.core.ui.theme.v1.SeaweedTheme
import com.zoewave.probase.seaweed.mobile.ui.components.SeaweedMainScreen
import com.zoewave.probase.seaweed.model.SeaweedThemeConfig
import com.zoewave.probase.seaweed.model.ThemeMode
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userSettingsRepository: UserSettingsRepository

    @Inject
    lateinit var categoryRepository: CategoryRepository

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val firebaseAnalytics = Firebase.analytics
        // Tags all future events from this user's phone
        firebaseAnalytics.setUserProperty("device_platform", "mobile")

        lifecycleScope.launch {
            categoryRepository.initializeDefaultCategories()
        }

        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            val userSettings by userSettingsRepository.getUserSettings().collectAsStateWithLifecycle(null)
            
            SeaweedTheme(
                themeConfig = userSettings?.themeConfig ?: SeaweedThemeConfig.DEFAULT,
                themeMode = userSettings?.themeMode ?: ThemeMode.SYSTEM
            ) {
                StripePaymentProvider(
                    onResult = { /* Global handler if needed */ }
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        SeaweedMainScreen(windowSizeClass = windowSizeClass)
                    }
                }
            }
        }
    }
}
