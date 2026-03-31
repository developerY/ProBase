package com.zoewave.probase.convention

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
                // Room runtime (KTX extensions are now merged into runtime in Room 3)
                add("implementation", libs.findLibrary("room.runtime").get())
                // Bundled SQLite driver for Room 3
                add("implementation", libs.findLibrary("sqlite.bundled").get())
                // Use KSP for Room's compiler instead of Kapt
                add("ksp", libs.findLibrary("room.compiler").get())
            }

            // Configure Room schema location for migrations
            extensions.configure<LibraryExtension> {
                defaultConfig {
                    // Export schemas to the 'schemas' folder within the module.
                    // This allows for database migration tracking via VCS.
                    javaCompileOptions {
                        annotationProcessorOptions {
                            arguments["room.schemaLocation"] = "$projectDir/schemas"
                        }
                    }
                }
            }

            // Configure KSP to export Room schemas
            extensions.configure<KspExtension> {
                // Ensure KSP also knows where to export schemas
                arg("room.schemaLocation", projectDir.path + "/schemas")
            }
        }
    }
}
