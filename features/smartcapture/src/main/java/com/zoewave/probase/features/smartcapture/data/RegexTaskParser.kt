package com.zoewave.probase.features.smartcapture.data

import com.zoewave.probase.features.smartcapture.domain.SmartTask
import javax.inject.Inject

class RegexTaskParser @Inject constructor() {

    fun parse(text: String): SmartTask {
        val lines = text.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) return SmartTask(rawText = text)

        val title = lines.first()
        val budget = extractBudget(text)
        val date = extractDate(text)

        return SmartTask(
            title = title,
            description = text.take(200),
            estimatedBudget = budget,
            dueDate = date,
            rawText = text
        )
    }

    private fun extractBudget(text: String): Double? {
        val regex = Regex("""\$?\s?(\d+([.,]\d{2})?)""")
        return regex.find(text)?.groupValues?.get(1)?.toDoubleOrNull()
    }

    private fun extractDate(text: String): String? {
        // Simple regex for common date formats (MM/DD or DD/MM)
        val regex = Regex("""(\d{1,2}[/-]\d{1,2}([/-]\d{2,4})?)""")
        return regex.find(text)?.value
    }
}
