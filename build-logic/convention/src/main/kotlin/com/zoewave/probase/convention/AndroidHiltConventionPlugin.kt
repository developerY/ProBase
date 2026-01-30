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

                // OPTIONAL: Better to keep this in feature modules, but if you need it:
                // Note: Updated alias to match our new TOML ("hilt-navigation-compose")
                // add("implementation", libs.findLibrary("hilt-navigation-compose").get())
            }
        }
    }
}