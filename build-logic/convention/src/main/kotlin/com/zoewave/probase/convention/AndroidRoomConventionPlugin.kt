package com.zoewave.probase.convention

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidRoomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            // 1. Apply KSP (Required for Room)
            pluginManager.apply("com.google.devtools.ksp")

            // 2. Configure Dependencies
            dependencies {
                add("implementation", libs.findLibrary("room.runtime").get())
                add("implementation", libs.findLibrary("room.ktx").get())
                add("ksp", libs.findLibrary("room.compiler").get())
            }

            // 3. Configure Schema Location (For exporting DB schemas)
            // This is critical for database migrations
            extensions.configure<LibraryExtension> {
                // We assume this plugin is only applied to Libraries (:core:data, :database, etc.)
                // If you use it in an App module, check for ApplicationExtension too.

                defaultConfig {
                    // Export schemas to 'schemas' folder in the module
                    javaCompileOptions {
                        annotationProcessorOptions {
                            arguments["room.schemaLocation"] = "$projectDir/schemas"
                        }
                    }
                }
            }
        }
    }
}