package com.zoewave.probase.convention

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            // 1. Apply the Base Android Plugin
            pluginManager.apply("com.android.application")

            // 2. Configure the Application Extension
            extensions.configure<ApplicationExtension> {
                // Apply Shared Base Logic (SDKs, Java 21, Kotlin)
                configureKotlinAndroid(this)

                // Apply Shared Build Types (ProGuard, Minification)
                configureBuildTypes(this)

                // 3. Application-Specific Configuration
                // Target SDK is an App-only property (Libraries don't typically set this)
                defaultConfig.targetSdk = libs.findVersion("android-targetSdk").get().toString().toInt()

                // 4. App-Specific Build Type Settings
                // 'isShrinkResources' is NOT in CommonExtension, so we set it here.
                val release = buildTypes.getByName("release")
                release.isShrinkResources = providers.gradleProperty("isShrinkResources")
                    .getOrElse("false")
                    .toBoolean()

                // 5. Packaging Options
                packaging.resources.excludes.add("/META-INF/{AL2.0,LGPL2.1}")
            }
        }
    }
}
