package com.zoewave.probase.convention

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * A convention plugin for Google Firebase integration (Analytics and Crashlytics).
 * Applies 'com.google.gms.google-services' and 'com.google.firebase.crashlytics' plugins
 * and adds the necessary dependencies using the Firebase BOM.
 */
class AndroidApplicationFirebaseConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.google.gms.google-services")
                apply("com.google.firebase.crashlytics")
            }

            dependencies {
                val bom = libs.findLibrary("firebase-bom").get()
                add("implementation", platform(bom))
                add("implementation", libs.findLibrary("firebase-analytics").get())
                add("implementation", libs.findLibrary("firebase-crashlytics").get())
            }
        }
    }
}
