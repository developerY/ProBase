package com.zoewave.probase.rxlogic.model.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface RxLogicRoute {
    @Serializable
    data object Main : RxLogicRoute

    @Serializable
    data object Medications : RxLogicRoute

    @Serializable
    data class MedicationDetail(val medicationId: String) : RxLogicRoute

    @Serializable
    data object Settings : RxLogicRoute
}
