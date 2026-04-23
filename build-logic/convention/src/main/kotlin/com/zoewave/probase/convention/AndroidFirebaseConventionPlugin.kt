package com.zoewave.probase.convention

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * A common convention plugin for Google Firebase dependencies.
 * Adds the necessary dependencies using the Firebase BOM, but DOES NOT apply
 * the 'google-services' or 'crashlytics' plugins (which are app-module only).
 */
class AndroidFirebaseConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            configureFirebase()
        }
    }
}
