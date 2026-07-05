package com.zoewave.probase.convention

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension

/**
 * Shared configuration logic for Jetpack Compose features.
 * Used in both Application and Library Compose convention plugins.
 */
internal fun Project.configureAndroidCompose(
    commonExtension: CommonExtension,
) {
    commonExtension.apply {
        // AGP 9: Must use dot syntax for buildFeatures.compose
        buildFeatures.compose = true

        dependencies {
            // Apply the BOM to manage all Compose library versions
            val bom = libs.findLibrary("androidx-compose-bom").get()
            add("implementation", platform(bom))
            add("androidTestImplementation", platform(bom))

            // Core Compose UI dependencies
            add("implementation", libs.findLibrary("androidx-activity-compose").get())
            add("implementation", libs.findLibrary("androidx-compose-ui").get())
            add("implementation", libs.findLibrary("androidx-compose-ui-graphics").get())
            add("implementation", libs.findLibrary("androidx-compose-ui-tooling-preview").get())
            add("implementation", libs.findLibrary("androidx-compose-material3").get())

            // Debug tooling and testing manifest
            add("debugImplementation", libs.findLibrary("androidx-compose-ui-tooling").get())
            add("debugImplementation", libs.findLibrary("androidx-compose-ui-test-manifest").get())

            // Instrumentation testing with JUnit4
            add("androidTestImplementation", libs.findLibrary("androidx-compose-ui-test-junit4").get())
        }
    }

    extensions.configure<ComposeCompilerGradlePluginExtension> {
        // includeSourceInformation = true // Optional: good for debugging
        // metricsDestination = layout.buildDirectory.dir("compose_metrics")
        // reportsDestination = layout.buildDirectory.dir("compose_reports")
    }
}
