package com.zoewave.probase.convention

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidHiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.google.devtools.ksp")
                apply("com.google.dagger.hilt.android")
            }

            dependencies {
                add("implementation", libs.findLibrary("hilt-android").get())
                add("ksp", libs.findLibrary("hilt-android-compiler").get())

                // OPTIONAL: I removed hilt-navigation-compose from here.
                // It is better to add that only in modules that actually use Compose UI.
                // If you want it everywhere, you can uncomment the line below:
                // add("implementation", libs.findLibrary("androidx-hilt-navigation-compose").get())
            }
        }
    }
}