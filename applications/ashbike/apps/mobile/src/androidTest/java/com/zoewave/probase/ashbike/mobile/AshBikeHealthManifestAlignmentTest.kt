package com.zoewave.probase.ashbike.mobile

import android.content.pm.PackageManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.zoewave.probase.ashbike.mobile.di.AshBikeHealthPermissionPolicy
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AshBikeHealthManifestAlignmentTest {

    @Test
    fun manifestHealthPermissionsExactMatchRuntimePolicy() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val packageInfo = context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_PERMISSIONS
        )
        val requestedPermissions = packageInfo.requestedPermissions?.toList() ?: emptyList()
        val manifestHealthPermissions = requestedPermissions
            .filter { it.startsWith("android.permission.health.") }
            .toSet()

        val policy = AshBikeHealthPermissionPolicy()
        val runtimePermissions = policy.permissions

        assertEquals(
            "Health Connect permissions in AndroidManifest.xml must exactly match AshBikeHealthPermissionPolicy",
            runtimePermissions,
            manifestHealthPermissions
        )
    }
}
