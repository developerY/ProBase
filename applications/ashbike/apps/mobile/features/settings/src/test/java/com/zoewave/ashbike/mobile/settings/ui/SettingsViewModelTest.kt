package com.zoewave.ashbike.mobile.settings.ui

import android.util.Log
import com.zoewave.ashbike.model.bike.LocationEnergyLevel
import com.zoewave.probase.ashbike.database.ProfileData
import com.zoewave.probase.ashbike.database.repository.AppSettingsRepository
import com.zoewave.probase.ashbike.database.repository.UserProfileRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private lateinit var viewModel: SettingsViewModel
    private val appRepo: AppSettingsRepository = mockk()
    private val profileRepo: UserProfileRepository = mockk()
    private val testDispatcher = kotlinx.coroutines.test.UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0

        every { appRepo.themeFlow } returns flowOf("System")
        every { appRepo.languageFlow } returns flowOf("English")
        every { appRepo.notificationsFlow } returns flowOf("Enabled")
        every { appRepo.unitsFlow } returns flowOf("Metric")
        every { appRepo.gpsAccuracyFlow } returns flowOf(LocationEnergyLevel.BALANCED)
        every { appRepo.longRideEnabledFlow } returns flowOf(false)
        every { appRepo.crashlyticsEnabledFlow } returns flowOf(true)
        every { appRepo.analyticsEnabledFlow } returns flowOf(true)

        every { profileRepo.nameFlow } returns flowOf("John")
        every { profileRepo.heightFlow } returns flowOf("180")
        every { profileRepo.weightFlow } returns flowOf("75")
        every { profileRepo.profileReviewedOrSavedFlow } returns flowOf(false)

        viewModel = SettingsViewModel(appRepo, profileRepo)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `onEvent UpdateSetting updates repository`() = runTest {
        coEvery { appRepo.setTheme("Dark") } returns Unit

        viewModel.onEvent(SettingsEvent.UpdateSetting("Theme", "Dark"))

        coVerify { appRepo.setTheme("Dark") }
    }

    @Test
    fun `onEvent SaveProfile updates profile in repository`() = runTest {
        coEvery { profileRepo.saveProfile(any()) } returns Unit

        val profile = ProfileData("Jane", "170", "65")
        viewModel.onEvent(SettingsEvent.SaveProfile(profile))

        coVerify { profileRepo.saveProfile(profile) }
    }

    @Test
    fun `onEvent UpdateEnergyLevel sets locally then repository`() = runTest {
        coEvery { appRepo.setGpsAccuracy(any()) } returns Unit

        viewModel.onEvent(SettingsEvent.UpdateEnergyLevel(LocationEnergyLevel.HIGH_ACCURACY))

        coVerify { appRepo.setGpsAccuracy(LocationEnergyLevel.HIGH_ACCURACY.name) }
    }

    @Test
    fun `onEvent UpdateCrashlyticsEnabled updates repository`() = runTest {
        coEvery { appRepo.setCrashlyticsEnabled(false) } returns Unit

        viewModel.onEvent(SettingsEvent.UpdateCrashlyticsEnabled(false))

        coVerify { appRepo.setCrashlyticsEnabled(false) }
    }

    @Test
    fun `onEvent UpdateAnalyticsEnabled updates repository`() = runTest {
        coEvery { appRepo.setAnalyticsEnabled(false) } returns Unit

        viewModel.onEvent(SettingsEvent.UpdateAnalyticsEnabled(false))

        coVerify { appRepo.setAnalyticsEnabled(false) }
    }
}
