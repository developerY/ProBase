package com.zoewave.probase.gotmind.analytics

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import com.google.firebase.installations.FirebaseInstallations
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAnalyticsHelper @Inject constructor(
    private val firebaseAnalytics: FirebaseAnalytics
) : AnalyticsHelper {

    override fun logEvent(event: AnalyticsEvent) {
        firebaseAnalytics.logEvent(event.type) {
            for (param in event.extras) {
                param(param.key, param.value)
            }
        }
    }

    override suspend fun getFirebaseId(): String {
        return try {
            FirebaseInstallations.getInstance().id.await()
        } catch (e: Exception) {
            "Unknown"
        }
    }
}
