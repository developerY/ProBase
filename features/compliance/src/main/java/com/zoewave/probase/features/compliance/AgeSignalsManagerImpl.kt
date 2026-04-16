package com.zoewave.probase.features.compliance

import android.content.Context
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.play.agesignals.model.AgeSignalsErrorCode
import com.google.android.play.agesignals.AgeSignalsException
import com.google.android.play.agesignals.AgeSignalsManagerFactory
import com.google.android.play.agesignals.AgeSignalsRequest
import com.google.android.play.agesignals.AgeSignalsResult
import com.google.android.play.agesignals.model.AgeSignalsVerificationStatus
import com.zoewave.probase.features.compliance.model.AgeRange
import com.zoewave.probase.features.compliance.model.AgeSignal
import com.zoewave.probase.features.compliance.model.AgeVerificationStatus
import com.zoewave.probase.features.compliance.model.ComplianceError
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class AgeSignalsManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AgeSignalsManager {

    private val playAgeSignalsManager = AgeSignalsManagerFactory.create(context)

    override suspend fun getAgeSignal(): Result<AgeSignal> {
        return try {
            val request = AgeSignalsRequest.builder().build()
            val result = playAgeSignalsManager.checkAgeSignals(request).await()
            Result.success(mapToAgeSignal(result))
        } catch (e: Exception) {
            Result.failure(mapToComplianceError(e))
        }
    }

    private fun mapToAgeSignal(result: AgeSignalsResult): AgeSignal {
        return AgeSignal(
            ageRange = mapAgeRange(result.ageLower(), result.ageUpper()),
            verificationStatus = mapVerificationStatus(result.userStatus()),
            mostRecentApprovalDate = result.mostRecentApprovalDate()
        )
    }

    private fun mapAgeRange(lower: Int?, upper: Int?): AgeRange? {
        return when {
            lower == 0 && upper == 12 -> AgeRange.AGE_0_12
            lower == 13 && upper == 15 -> AgeRange.AGE_13_15
            lower == 16 && upper == 17 -> AgeRange.AGE_16_17
            lower == 18 -> AgeRange.AGE_18_PLUS
            else -> AgeRange.UNKNOWN
        }
    }

    private fun mapVerificationStatus(status: Int?): AgeVerificationStatus {
        return when (status) {
            AgeSignalsVerificationStatus.VERIFIED -> AgeVerificationStatus.VERIFIED
            AgeSignalsVerificationStatus.DECLARED -> AgeVerificationStatus.DECLARED
            AgeSignalsVerificationStatus.SUPERVISED -> AgeVerificationStatus.SUPERVISED
            AgeSignalsVerificationStatus.SUPERVISED_APPROVAL_PENDING -> AgeVerificationStatus.SUPERVISED_APPROVAL_PENDING
            AgeSignalsVerificationStatus.SUPERVISED_APPROVAL_DENIED -> AgeVerificationStatus.SUPERVISED_APPROVAL_DENIED
            else -> AgeVerificationStatus.UNKNOWN
        }
    }

    private fun mapToComplianceError(e: Exception): ComplianceError {
        return when (e) {
            is AgeSignalsException -> {
                when (e.statusCode) {
                    AgeSignalsErrorCode.SDK_VERSION_OUTDATED -> ComplianceError.SdkVersionOutdated
                    else -> ComplianceError.GenericError(e.message)
                }
            }
            is ApiException -> {
                when (e.statusCode) {
                    CommonStatusCodes.NETWORK_ERROR -> ComplianceError.NetworkError
                    else -> ComplianceError.GenericError(e.message)
                }
            }
            else -> ComplianceError.GenericError(e.message)
        }
    }
}
