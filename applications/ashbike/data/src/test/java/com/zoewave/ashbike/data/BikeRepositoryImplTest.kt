package com.zoewave.ashbike.data

import com.zoewave.ashbike.data.repository.bike.BikeRepositoryImpl
import com.zoewave.ashbike.model.bike.SuspensionState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BikeRepositoryImplTest {

    private lateinit var repo: BikeRepositoryImpl

    @Before
    fun setup() {
        repo = BikeRepositoryImpl()
    }

    @Test
    fun `initial gear is 1 and gearUp increases gear count up to 12`() = runTest {
        assertEquals(1, repo.currentGear.first())

        repo.gearUp()
        assertEquals(2, repo.currentGear.first())

        repo.setGear(12)
        assertEquals(12, repo.currentGear.first())

        // Cannot exceed max gear 12
        repo.gearUp()
        assertEquals(12, repo.currentGear.first())
    }

    @Test
    fun `gearDown decreases gear count down to 1`() = runTest {
        repo.setGear(3)
        assertEquals(3, repo.currentGear.first())

        repo.gearDown()
        assertEquals(2, repo.currentGear.first())

        repo.gearDown()
        assertEquals(1, repo.currentGear.first())

        // Cannot drop below gear 1
        repo.gearDown()
        assertEquals(1, repo.currentGear.first())
    }

    @Test
    fun `toggleSuspension cycles through OPEN, TRAIL, and LOCK`() = runTest {
        assertEquals(SuspensionState.OPEN, repo.suspensionState.first())

        repo.toggleSuspension()
        assertEquals(SuspensionState.TRAIL, repo.suspensionState.first())

        repo.toggleSuspension()
        assertEquals(SuspensionState.LOCK, repo.suspensionState.first())

        repo.toggleSuspension()
        assertEquals(SuspensionState.OPEN, repo.suspensionState.first())
    }

    @Test
    fun `glass session cannot be active if hardware is disconnected`() = runTest {
        assertFalse(repo.isGlassConnected.first())

        // Request active session while disconnected should be rejected
        repo.updateGlassSessionState(true)
        assertFalse(repo.isGlassSessionActive.first())

        // Connect hardware, then activate session
        repo.updateGlassConnectionState(true)
        assertTrue(repo.isGlassConnected.first())

        repo.updateGlassSessionState(true)
        assertTrue(repo.isGlassSessionActive.first())

        // Disconnecting hardware forces session to false
        repo.updateGlassConnectionState(false)
        assertFalse(repo.isGlassSessionActive.first())
    }

    @Test
    fun `updateConnectionState sets isBikeConnected`() = runTest {
        assertFalse(repo.isBikeConnected.first())

        repo.updateConnectionState(true)
        assertTrue(repo.isBikeConnected.first())

        repo.updateConnectionState(false)
        assertFalse(repo.isBikeConnected.first())
    }

    @Test
    fun `updateRideInfo emits new info`() = runTest {
        val initial = repo.rideInfo.first()

        val newInfo = initial.copy(currentSpeed = 25.0)
        repo.updateRideInfo(newInfo)

        val updated = repo.rideInfo.first()
        assertEquals(25.0, updated.currentSpeed, 0.01)
    }
}
