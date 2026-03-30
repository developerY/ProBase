package com.zoewave.probase.convention

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/**
 * Shared configuration logic for both Android Library and Application modules.
 * Sets the compile and min SDKs, Java toolchain versions, and Kotlin compiler options.
 */
internal fun Project.configureKotlinAndroid(
    commonExtension: CommonExtension,
) {
    // Compile and Min SDK versions from the Version Catalog
    commonExtension.compileSdk = libs.findVersion("android-compileSdk").get().toString().toInt()
    commonExtension.defaultConfig.minSdk = libs.findVersion("android-minSdk").get().toString().toInt()

    // Align source and target compatibility with Java 21
    commonExtension.compileOptions.sourceCompatibility = JavaVersion.VERSION_21
    commonExtension.compileOptions.targetCompatibility = JavaVersion.VERSION_21

    // Align Kotlin compiler tasks with the JVM 21 target
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
    }
}

/**
 * Shared configuration for build types across Android modules.
 * Toggles minification for release and debug builds.
 */
internal fun Project.configureBuildTypes(
    commonExtension: CommonExtension,
) {
    val release = commonExtension.buildTypes.getByName("release")

    // Use a project property to toggle minification for release builds safely
    release.isMinifyEnabled = providers.gradleProperty("isMinifyForRelease")
        .getOrElse("false")
        .toBoolean()

    // ProGuard optimization rules for release builds
    release.proguardFiles(
        commonExtension.getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
    )

    // Ensure minification is disabled for debug builds
    val debug = commonExtension.buildTypes.getByName("debug")
    debug.isMinifyEnabled = false
}
