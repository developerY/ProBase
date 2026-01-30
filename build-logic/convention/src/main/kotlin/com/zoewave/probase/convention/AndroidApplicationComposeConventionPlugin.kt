package com.zoewave.probase.convention

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class AndroidApplicationComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            // Apply the Compose Compiler Plugin (Critical for Kotlin 2.0+)
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            val extension = extensions.getByType<ApplicationExtension>()
            configureAndroidCompose(extension)

            // ✅ ADD THIS BLOCK
            // Applications always need 'activity-compose' to set the content view.
            dependencies {
                add("implementation", libs.findLibrary("androidx-activity-compose").get())
            }
        }
    }
}