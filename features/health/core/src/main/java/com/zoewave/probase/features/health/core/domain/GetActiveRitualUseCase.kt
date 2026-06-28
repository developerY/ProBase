package com.zoewave.probase.features.health.core.domain

import com.zoewave.probase.core.model.ritual.BeautyRoutine
import com.zoewave.probase.core.model.ritual.RoutineTime
import java.util.Calendar
import javax.inject.Inject

class GetActiveRitualUseCase @Inject constructor() {
    operator fun invoke(routines: List<BeautyRoutine>): ActiveRitual {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        
        val morning = routines.find { it.time == RoutineTime.MORNING }
        val meals = routines.find { it.time == RoutineTime.MEALS }
        val evening = routines.find { it.time == RoutineTime.EVENING }

        return when {
            hour in 5..9 -> ActiveRitual(
                routine = morning,
                title = "Morning Ritual",
                description = "Prepare for a balanced day ahead.",
                isDaytime = true
            )
            hour in 10..19 -> ActiveRitual(
                routine = meals,
                title = "Meals Ritual",
                description = "Nourish your metabolism with precise biochemical timing.",
                isDaytime = true
            )
            else -> ActiveRitual(
                routine = evening,
                title = "Evening Ritual",
                description = "Every step is an act of self-love.",
                isDaytime = false
            )
        }
    }
}
