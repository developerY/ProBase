package com.zoewave.probase.features.health.core.ui

import android.util.Log
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.HydrationRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import com.google.common.truth.Truth.assertThat
import com.zoewave.probase.core.data.repository.HydrationSettings
import com.zoewave.probase.core.data.repository.bluetoothLE.BluetoothLeRepository
import com.zoewave.probase.core.data.service.health.HealthSessionManager
import com.zoewave.probase.core.model.ble.GattConnectionState
import com.zoewave.probase.features.health.core.WellnessCorrelationEngine
import com.zoewave.probase.features.health.core.di.DefaultHealthPermissionPolicy
import com.zoewave.probase.features.health.core.di.ResolvedHealthPermissionPolicy
import com.zoewave.probase.features.health.core.domain.SyncRideUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HealthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val healthSessionManager: HealthSessionManager = mockk(relaxed = true)
    private val wellnessEngine: WellnessCorrelationEngine = mockk(relaxed = true)
    private val syncRideUseCase: SyncRideUseCase = mockk(relaxed = true)
    private val bleRepository: BluetoothLeRepository = mockk(relaxed = true)
    private val hydrationSettings: HydrationSettings = mockk(relaxed = true)
    private val testPermissions = setOf("android.permission.health.READ_STEPS", "android.permission.health.WRITE_EXERCISE")
    private val resolvedPolicy = ResolvedHealthPermissionPolicy(testPermissions)

    private lateinit var viewModel: HealthViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.e(any(), any<String>()) } returns 0

        every { healthSessionManager.availability } returns MutableStateFlow(0)
        every { bleRepository.gattConnectionState } returns MutableStateFlow(GattConnectionState.Disconnected)
        every { hydrationSettings.hydrationGoalFlow } returns flowOf(2.5)

        viewModel = HealthViewModel(
            healthSessionManager,
            wellnessEngine,
            syncRideUseCase,
            bleRepository,
            hydrationSettings,
            resolvedPolicy
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialUiStateIsUninitialized() {
        val state = viewModel.uiState.value
        assertThat(state).isInstanceOf(HealthUiState.Uninitialized::class.java)
    }

    @Test
    fun permissions_exposesInjectedPolicySet() {
        // The ViewModel must expose exactly the per-app set Hilt injects; each app's own
        // coverage (writes for sync, reads for the dashboard) is guarded in that app module.
        assertThat(viewModel.permissions).isEqualTo(testPermissions)
    }

    @Test
    fun defaultPolicy_preservesWellnessBaseline() {
        // Fallback for apps without their own binding: the historical 6-permission set.
        // If this intentionally changes, update the affected apps' Play declarations too.
        assertThat(DefaultHealthPermissionPolicy().permissions).contains(HealthPermission.getReadPermission(StepsRecord::class))
        assertThat(DefaultHealthPermissionPolicy().permissions).contains(HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class))
        assertThat(DefaultHealthPermissionPolicy().permissions).contains(HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class))
        assertThat(DefaultHealthPermissionPolicy().permissions).contains(HealthPermission.getReadPermission(SleepSessionRecord::class))
        assertThat(DefaultHealthPermissionPolicy().permissions).contains(HealthPermission.getReadPermission(HydrationRecord::class))
        assertThat(DefaultHealthPermissionPolicy().permissions).contains(HealthPermission.getWritePermission(HydrationRecord::class))
    }
}
