package com.zoewave.probase.goswift.mobile.healthsync.ui

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HealthSyncViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `initial state has correct default values`() {
        val viewModel = HealthSyncViewModel()
        val state = viewModel.uiState.value

        assertThat(state.isSdkAvailable).isFalse()
        assertThat(state.isGranted).isFalse()
        assertThat(state.latestHeartRateBpm).isNull()
        assertThat(state.latestHrvMs).isNull()
        assertThat(state.totalSleepMinutes).isNull()
        assertThat(state.isLoading).isTrue()
    }

    @Test
    fun `requiredPermissions does not include StepsRecord`() {
        val viewModel = HealthSyncViewModel()
        val stepsPermission = HealthPermission.getReadPermission(StepsRecord::class)

        assertThat(viewModel.requiredPermissions).doesNotContain(stepsPermission)
    }

    @Test
    fun `loadRealHealthData updates state when SDK is unavailable`() = runTest(testDispatcher) {
        mockkObject(HealthConnectClient.Companion)
        val mockContext = mockk<Context>()
        every { HealthConnectClient.getSdkStatus(mockContext) } returns HealthConnectClient.SDK_UNAVAILABLE

        val viewModel = HealthSyncViewModel()
        viewModel.loadRealHealthData(mockContext)

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.isSdkAvailable).isFalse()
        assertThat(state.isGranted).isFalse()
        assertThat(state.isLoading).isFalse()
    }
}
