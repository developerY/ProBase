package com.zoewave.probase.ashbike.mobile

import com.zoewave.probase.ashbike.mobile.di.AshBikeHealthPermissionPolicy
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AshBikeHealthManifestJvmTest {

    @Test
    fun mergedManifestContainsAllHealthPermissionsInRuntimePolicy() {
        val manifestPath = "build/intermediates/merged_manifests/debug/processDebugManifest/AndroidManifest.xml"
        val manifestFile = File(manifestPath)

        // Only run this test if the manifest exists (e.g. running from Android Studio or after processDebugMainManifest)
        if (!manifestFile.exists()) {
            println("Skipping test: Merged manifest not found at $manifestPath")
            return
        }

        val manifestContent = manifestFile.readText()
        val policy = AshBikeHealthPermissionPolicy()
        val runtimePermissions = policy.permissions

        val missingPermissions = mutableListOf<String>()

        for (permission in runtimePermissions) {
            // Check if the permission is declared in the manifest
            val declared = manifestContent.contains("<uses-permission android:name=\"$permission\"")
            if (!declared) {
                missingPermissions.add(permission)
            }
        }

        assertTrue(
            "The following Health Connect permissions are required by AshBikeHealthPermissionPolicy " +
                    "but are missing from the merged AndroidManifest.xml: $missingPermissions",
            missingPermissions.isEmpty()
        )
    }
}
