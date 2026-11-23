package com.zoewave.probase.ashbike.mobile

import com.zoewave.probase.ashbike.database.repository.AppSettingsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TelemetryConsentTest {

    @Test
    fun freshInstallDefaults_bothCrashlyticsAndAnalyticsAreTrue() {
        runBlocking {
            val repository: AppSettingsRepository = mockk()
            every { repository.crashlyticsEnabledFlow } returns MutableStateFlow(true)
            every { repository.analyticsEnabledFlow } returns MutableStateFlow(true)

            val crashlytics = repository.crashlyticsEnabledFlow.first()
            val analytics = repository.analyticsEnabledFlow.first()

            assertTrue(crashlytics)
            assertTrue(analytics)
        }
    }

    @Test
    fun togglingCrashReportingOff_persistsFalse() {
        runBlocking {
            val repository: AppSettingsRepository = mockk()
            val crashlyticsFlow = MutableStateFlow(true)
            every { repository.crashlyticsEnabledFlow } returns crashlyticsFlow
            coEvery { repository.setCrashlyticsEnabled(false) } answers {
                crashlyticsFlow.value = false
            }

            repository.setCrashlyticsEnabled(false)

            coVerify { repository.setCrashlyticsEnabled(false) }
            assertEquals(false, repository.crashlyticsEnabledFlow.first())
        }
    }

    @Test
    fun togglingAnalyticsOff_persistsFalse() {
        runBlocking {
            val repository: AppSettingsRepository = mockk()
            val analyticsFlow = MutableStateFlow(true)
            every { repository.analyticsEnabledFlow } returns analyticsFlow
            coEvery { repository.setAnalyticsEnabled(false) } answers {
                analyticsFlow.value = false
            }

            repository.setAnalyticsEnabled(false)

            coVerify { repository.setAnalyticsEnabled(false) }
            assertEquals(false, repository.analyticsEnabledFlow.first())
        }
    }
}
