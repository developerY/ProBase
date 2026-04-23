package com.zoewave.probase.convention

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.kotlin

/**
 * A convention plugin for Android Library modules.
 * Applies the 'com.android.library' plugin and configures shared logic for SDKs, Kotlin, and Test Runners.
 */
class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            // Apply the base Android Library plugin
            pluginManager.apply("com.android.library")

            extensions.configure<LibraryExtension> {
                // Shared base logic for SDKs, Java 21, and Kotlin
                configureKotlinAndroid(this)

                // Target SDK is also relevant for libraries if we want to ensure behavior
                // Note: compileSdk is handled in configureKotlinAndroid

                // Standard Android library test runner configuration
                defaultConfig.testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                testOptions.animationsDisabled = true

                // Resource prefixing to prevent collisions when library modules are merged
                resourcePrefix = path.split("""\W""".toRegex())
                    .drop(1)
                    .distinct()
                    .joinToString(separator = "_")
                    .lowercase() + "_"
            }

            dependencies {
                // Standard Kotlin testing dependencies
                add("testImplementation", kotlin("test"))
                add("androidTestImplementation", kotlin("test"))
            }
        }
    }
}
