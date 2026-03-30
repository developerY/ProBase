package com.zoewave.probase.convention

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class FeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            dependencies {
                // Add common feature dependencies here.
                // For example:
                // add("implementation", project(":core:ui"))
                // add("implementation", project(":core:model"))
            }
        }
    }
}
