package com.zoewave.probase.convention

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import com.google.devtools.ksp.gradle.KspExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/**
 * A convention plugin for Room database integration.
 * Applies the KSP plugin and configures Room runtime, compiler, and schema exporting.
 */
class AndroidRoomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            // Apply KSP (Required for modern Room processing)
            pluginManager.apply("com.google.devtools.ksp")

            // Room dependencies from the Version Catalog
            dependencies {
                add("implementation", libs.findLibrary("room.runtime").get())
                add("implementation", libs.findLibrary("sqlite.bundled").get())
                add("ksp", libs.findLibrary("room.compiler").get())
            }

            // Configure Room schema location for migrations
            pluginManager.withPlugin("com.android.library") {
                extensions.configure<LibraryExtension> {
                    defaultConfig.javaCompileOptions.annotationProcessorOptions {
                        arguments["room.schemaLocation"] = "$projectDir/schemas"
                    }
                }
            }
            pluginManager.withPlugin("com.android.application") {
                extensions.configure<ApplicationExtension> {
                    defaultConfig.javaCompileOptions.annotationProcessorOptions {
                        arguments["room.schemaLocation"] = "$projectDir/schemas"
                    }
                }
            }

            // Configure KSP to export Room schemas
            extensions.configure<KspExtension> {
                arg("room.schemaLocation", projectDir.path + "/schemas")
            }
        }
    }
}
