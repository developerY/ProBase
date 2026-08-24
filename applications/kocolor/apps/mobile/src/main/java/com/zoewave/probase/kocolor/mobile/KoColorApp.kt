package com.zoewave.probase.kocolor.mobile

import android.app.Application
import com.zoewave.probase.features.ai.firebase.AppCheckInitializer
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class KoColorApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppCheckInitializer.initialize(this)
    }
}
