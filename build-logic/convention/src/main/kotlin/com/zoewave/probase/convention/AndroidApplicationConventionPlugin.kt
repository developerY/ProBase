package com.zoewave.probase.convention

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * A convention plugin for Android Application modules.
 * Applies the 'com.android.application' plugin and configures shared logic for SDKs, Kotlin, and Build Types.
 */
class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            // Apply the base Android Application plugin
            pluginManager.apply("com.android.application")

            extensions.configure<ApplicationExtension> {
                // Shared base logic for SDKs, Java 21, and Kotlin
                configureKotlinAndroid(this)

                // Shared build types configuration (ProGuard, Minification)
                configureBuildTypes(this)

                // Target SDK is an application-only property
                defaultConfig.targetSdk = libs.findVersionInt("android-targetSdk")

                // Application-specific build type settings
                val release = buildTypes.getByName("release")
                release.isShrinkResources = providers.gradleProperty("isShrinkResources")
                    .getOrElse("false")
                    .toBoolean()

                // Standard packaging exclusions for clean release builds
                packaging.resources.excludes.add("/META-INF/{AL2.0,LGPL2.1}")
                
                // Enable 16 KB page size support
                packaging.jniLibs.useLegacyPackaging = false
            }
        }
    }
}
