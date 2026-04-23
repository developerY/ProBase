package com.zoewave.probase.convention

import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureFirebase() {
    dependencies {
        val bom = libs.findLibrary("firebase-bom").get()
        add("implementation", platform(bom))
        add("implementation", libs.findLibrary("firebase-analytics").get())
        add("implementation", libs.findLibrary("firebase-crashlytics").get())
        add("implementation", libs.findLibrary("firebase-installations").get())
        add("implementation", libs.findLibrary("kotlinx-coroutines-play-services").get())
    }
}
