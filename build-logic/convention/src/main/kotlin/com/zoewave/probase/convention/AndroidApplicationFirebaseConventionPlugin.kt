package com.zoewave.probase.convention

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * A convention plugin for Google Firebase integration (Analytics and Crashlytics).
 * Applies 'com.google.gms.google-services' and 'com.google.firebase.crashlytics' plugins
 * to APPLICATION modules and adds the necessary dependencies using the Firebase BOM.
 */
class AndroidApplicationFirebaseConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.google.gms.google-services")
                apply("com.google.firebase.crashlytics")
            }

            configureFirebase()
        }
    }
}
