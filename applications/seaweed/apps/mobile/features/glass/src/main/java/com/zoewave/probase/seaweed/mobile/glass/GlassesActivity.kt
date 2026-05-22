package com.zoewave.probase.seaweed.mobile.glass

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.ComposeUiFlags
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.xr.glimmer.GlimmerTheme
import androidx.xr.glimmer.googlefonts.createGoogleSansFlexTypography
import com.zoewave.probase.seaweed.data.FinancialRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class GlassesActivity : ComponentActivity() {

    @Inject
    lateinit var financialRepository: FinancialRepository

    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Recommended for XR glasses to handle initial focus correctly
        ComposeUiFlags.isInitialFocusOnFocusableAvailable = true

        setContent {
            GlimmerTheme(
                typography = createGoogleSansFlexTypography()
            ) {
                // Mandatory black background for additive displays
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                ) {
                    val profile by financialRepository.getFinancialProfile()
                        .collectAsStateWithLifecycle(initialValue = null)

                    profile?.let {
                        SeaweedGlassApp(it)
                    }
                }
            }
        }
    }
}
