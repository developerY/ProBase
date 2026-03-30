package com.zoewave.probase.convention

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * A convention plugin for integrating Dagger Hilt for dependency injection.
 * Applies the KSP and Hilt Android plugins and configures common dependencies.
 */
class AndroidHiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                // Apply the KSP plugin for Hilt's annotation processing
                apply("com.google.devtools.ksp")
                // Apply the base Hilt Android plugin
                apply("com.google.dagger.hilt.android")
            }

            dependencies {
                // Core Hilt dependencies for Android
                add("implementation", libs.findLibrary("hilt-android").get())
                add("ksp", libs.findLibrary("hilt-android-compiler").get())
            }
        }
    }
}
