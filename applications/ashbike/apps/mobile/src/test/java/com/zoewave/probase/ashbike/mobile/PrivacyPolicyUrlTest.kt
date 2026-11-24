package com.zoewave.probase.ashbike.mobile

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PrivacyPolicyUrlTest {

    @Test
    fun privacyPolicyUrl_isValidHttpsAndNotYlabz() {
        val url = MainActivity.PRIVACY_POLICY_URL
        assertTrue("URL must start with https://", url.startsWith("https://"))
        assertFalse("URL must not contain ylabz.com", url.contains("ylabz.com"))
        assertEquals(
            "https://github.com/developerY/ProBase/blob/main/applications/ashbike/docs/data_and_privacy/PRIVACY_POLICY_PUBLIC.md",
            url
        )
    }
}
