package com.zoewave.probase.ashbike.mobile

import android.content.pm.PackageManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.zoewave.probase.ashbike.mobile.di.AshBikeHealthPermissionPolicy
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AshBikeHealthManifestAlignmentTest {

    @Test
    fun manifestContainsAllHealthPermissionsInRuntimePolicy() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val packageInfo = context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_PERMISSIONS
        )
        val requestedPermissions = packageInfo.requestedPermissions?.toList() ?: emptyList()

        val policy = AshBikeHealthPermissionPolicy()
        val runtimePermissions = policy.permissions

        val missingPermissions = runtimePermissions.filter { it !in requestedPermissions }

        assertTrue(
            "The following Health Connect permissions are required by AshBikeHealthPermissionPolicy " +
                    "but are missing from the merged AndroidManifest.xml: $missingPermissions",
            missingPermissions.isEmpty()
        )
    }
}
