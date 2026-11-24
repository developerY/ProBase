package com.zoewave.probase.ashbike.mobile

import android.app.Application
import com.zoewave.probase.ashbike.mobile.telemetry.FirebaseTelemetryController
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Inject

@HiltAndroidApp
class AshBikeApp : Application() {

    @Inject
    lateinit var telemetryController: FirebaseTelemetryController

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        telemetryController.initTelemetryObservers(applicationScope)
    }
}
