package com.zoewave.probase.ashbike.mobile

import com.zoewave.probase.ashbike.mobile.di.AshBikeHealthPermissionPolicy
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class AshBikeHealthManifestJvmTest {

    @Test
    fun mergedManifestHealthPermissionsExactMatchRuntimePolicy() {
        val debugPath = "build/intermediates/merged_manifests/debug/processDebugManifest/AndroidManifest.xml"
        val releasePath = "build/intermediates/merged_manifest/release/processReleaseMainManifest/AndroidManifest.xml"

        var manifestFile = File(debugPath)
        if (!manifestFile.exists()) {
            manifestFile = File(releasePath)
        }

        if (!manifestFile.exists()) {
            println("Skipping test: Merged manifest not found at $debugPath")
            return
        }

        val manifestContent = manifestFile.readText()
        val regex = Regex("""<uses-permission android:name="(android\.permission\.health\.[^"]+)"""")
        val manifestHealthPermissions = regex.findAll(manifestContent)
            .map { it.groupValues[1] }
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
