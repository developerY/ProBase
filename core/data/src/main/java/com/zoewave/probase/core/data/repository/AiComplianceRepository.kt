package com.zoewave.probase.core.data.repository

import android.content.Intent
import android.net.Uri
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository to handle GenAI compliance logic, such as reporting bad outputs.
 */
@Singleton
class AiComplianceRepository @Inject constructor() {
    private companion object {
        const val GOOGLE_AI_REPORT_URL = "https://aistudio.google.com/app/u/1/feedback"
    }

    /**
     * Returns an intent to redirect the user to Google's official AI Studio reporting tool.
     */
    fun getReportIntent(): Intent {
        return Intent(Intent.ACTION_VIEW, Uri.parse(GOOGLE_AI_REPORT_URL))
    }
}
