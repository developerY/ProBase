package com.zoewave.probase.kocolor.features.routines.data

import com.zoewave.probase.kocolor.model.BeautyRoutine
import com.zoewave.probase.kocolor.model.RoutineTime
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for aligning beauty routines with circadian rhythms and environmental factors.
 */
@Singleton
class RoutineChronobiologyService @Inject constructor() {

    /**
     * Determines the optimal routine objective based on current time.
     */
    fun getCircadianObjective(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "Defense & UV Protection"
            in 12..17 -> "Hydration & Environmental Shield"
            in 18..23 -> "Deep Repair & Regeneration"
            else -> "Nighttime Recovery"
        }
    }

    /**
     * Recommends adjustments to a routine based on context factors.
     */
    fun recommendAdjustments(routine: BeautyRoutine, contextFactors: List<String>): List<String> {
        val adjustments = mutableListOf<String>()
        
        if (contextFactors.contains("High UV Index") && routine.time == RoutineTime.MORNING) {
            adjustments.add("Apply high-potency Vitamin C and reapply SPF every 2 hours.")
        }
        
        if (contextFactors.contains("Low Sleep") && routine.time == RoutineTime.MORNING) {
            adjustments.add("Include a depuffing eye treatment and caffeine-based serum.")
        }
        
        if (contextFactors.contains("High Stress")) {
            adjustments.add("Add a soothing niacinamide or centella asiatica step.")
        }

        return adjustments
    }

    /**
     * Calculates required wait time before sleep to prevent product transfer.
     */
    fun getSleepWaitTime(routine: BeautyRoutine): Int {
        if (routine.time != RoutineTime.EVENING) return 0
        // Heuristic: Sum of minWaitMinutes of all completed steps + 30m final set
        val totalWait = routine.steps.filter { it.isCompleted }.sumOf { it.minWaitMinutes }
        return kotlin.math.max(30, totalWait)
    }
}
