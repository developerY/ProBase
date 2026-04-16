package com.zoewave.probase.features.compliance

import android.content.Context
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import com.google.android.gms.tasks.Tasks
import com.google.android.play.agesignals.model.AgeSignalsErrorCode
import com.google.android.play.agesignals.AgeSignalsException
import com.google.android.play.agesignals.AgeSignalsManager
import com.google.android.play.agesignals.AgeSignalsManagerFactory
import com.google.android.play.agesignals.AgeSignalsRequest
import com.google.android.play.agesignals.AgeSignalsResult
import com.google.android.play.agesignals.model.AgeSignalsVerificationStatus
import com.zoewave.probase.features.compliance.model.AgeRange
import com.zoewave.probase.features.compliance.model.AgeVerificationStatus
import com.zoewave.probase.features.compliance.model.ComplianceError
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Date

class AgeSignalsManagerTest {

    private val context = mockk<Context>()
    private val playAgeSignalsManager = mockk<AgeSignalsManager>()
    private lateinit var ageSignalsManager: com.zoewave.probase.features.compliance.AgeSignalsManager

    @Before
    fun setUp() {
        mockkStatic(AgeSignalsManagerFactory::class)
        every { AgeSignalsManagerFactory.create(context) } returns playAgeSignalsManager
        ageSignalsManager = AgeSignalsManagerImpl(context)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `getAgeSignal returns success when Play Store returns valid result`() = runTest {
        // Arrange
        val now = Date()
        val mockResult = mockk<AgeSignalsResult> {
            every { ageLower() } returns 18
            every { ageUpper() } returns null
            every { userStatus() } returns AgeSignalsVerificationStatus.VERIFIED
            every { mostRecentApprovalDate() } returns now
        }
        every { playAgeSignalsManager.checkAgeSignals(any()) } returns Tasks.forResult(mockResult)

        // Act
        val result = ageSignalsManager.getAgeSignal()

        // Assert
        assertTrue(result.isSuccess)
        val ageSignal = result.getOrThrow()
        assertEquals(AgeRange.AGE_18_PLUS, ageSignal.ageRange)
        assertEquals(AgeVerificationStatus.VERIFIED, ageSignal.verificationStatus)
        assertEquals(now, ageSignal.mostRecentApprovalDate)
    }

    @Test
    fun `getAgeSignal returns SdkVersionOutdated error when SDK is outdated`() = runTest {
        // Arrange
        val exception = AgeSignalsException(AgeSignalsErrorCode.SDK_VERSION_OUTDATED)
        every { playAgeSignalsManager.checkAgeSignals(any()) } returns Tasks.forException(exception)

        // Act
        val result = ageSignalsManager.getAgeSignal()

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ComplianceError.SdkVersionOutdated)
    }

    @Test
    fun `getAgeSignal returns NetworkError when a network error occurs`() = runTest {
        // Arrange
        val exception = ApiException(Status(CommonStatusCodes.NETWORK_ERROR))
        every { playAgeSignalsManager.checkAgeSignals(any()) } returns Tasks.forException(exception)

        // Act
        val result = ageSignalsManager.getAgeSignal()

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ComplianceError.NetworkError)
    }

    @Test
    fun `getAgeSignal returns GenericError for unknown exceptions`() = runTest {
        // Arrange
        val exception = RuntimeException("Something went wrong")
        every { playAgeSignalsManager.checkAgeSignals(any()) } returns Tasks.forException(exception)

        // Act
        val result = ageSignalsManager.getAgeSignal()

        // Assert
        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is ComplianceError.GenericError)
        assertEquals("Something went wrong", (error as ComplianceError.GenericError).originalMessage)
    }
}
