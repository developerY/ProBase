package com.zoewave.probase.ashbike.mobile.telemetry

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.zoewave.probase.ashbike.database.repository.AppSettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

interface FirebaseTelemetryController {
    fun initTelemetryObservers(scope: CoroutineScope)
    fun setCrashlyticsCollectionEnabled(enabled: Boolean)
    fun setAnalyticsCollectionEnabled(enabled: Boolean)
}

@Singleton
class FirebaseTelemetryControllerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: AppSettingsRepository
) : FirebaseTelemetryController {

    override fun initTelemetryObservers(scope: CoroutineScope) {
        scope.launch(Dispatchers.Main) {
            repository.crashlyticsEnabledFlow.collectLatest { enabled ->
                setCrashlyticsCollectionEnabled(enabled)
            }
        }
        scope.launch(Dispatchers.Main) {
            repository.analyticsEnabledFlow.collectLatest { enabled ->
                setAnalyticsCollectionEnabled(enabled)
            }
        }
    }

    override fun setCrashlyticsCollectionEnabled(enabled: Boolean) {
        try {
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(enabled)
        } catch (e: Exception) {
            // Safe fallback for unit testing environments without Firebase initialization
        }
    }

    override fun setAnalyticsCollectionEnabled(enabled: Boolean) {
        try {
            FirebaseAnalytics.getInstance(context).setAnalyticsCollectionEnabled(enabled)
        } catch (e: Exception) {
            // Safe fallback for unit testing environments without Firebase initialization
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class FirebaseTelemetryModule {

    @Binds
    @Singleton
    abstract fun bindFirebaseTelemetryController(
        impl: FirebaseTelemetryControllerImpl
    ): FirebaseTelemetryController
}
