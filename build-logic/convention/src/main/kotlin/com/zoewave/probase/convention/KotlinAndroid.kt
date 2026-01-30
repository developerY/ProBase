package com.zoewave.probase.convention

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/**
 * 1. Base Android & Kotlin Configuration
 * Applied to BOTH Libraries and Applications.
 */
internal fun Project.configureKotlinAndroid(
    commonExtension: CommonExtension,
) {
    // 1. Compile SDK (Direct Property Access)
    commonExtension.compileSdk = libs.findVersion("android-compileSdk").get().toString().toInt()

    // 2. Min SDK (Direct Property Access on defaultConfig)
    commonExtension.defaultConfig.minSdk = libs.findVersion("android-minSdk").get().toString().toInt()

    // 3. Java 21 Toolchain (Direct Property Access on compileOptions)
    commonExtension.compileOptions.sourceCompatibility = JavaVersion.VERSION_21
    commonExtension.compileOptions.targetCompatibility = JavaVersion.VERSION_21

    // 4. Align Kotlin compiler with JVM 21
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
    }
}

/**
 * 2. Build Type Configuration
 * Separated so we can toggle minification based on the module type.
 */
internal fun Project.configureBuildTypes(
    commonExtension: CommonExtension,
) {
    // 1. Configure Release (Get object -> Set properties)
    val release = commonExtension.buildTypes.getByName("release")

    // Use providers.gradleProperty for safe caching support
    release.isMinifyEnabled = providers.gradleProperty("isMinifyForRelease")
        .getOrElse("false")
        .toBoolean()

    release.proguardFiles(
        commonExtension.getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
    )

    // 2. Configure Debug (Get object -> Set properties)
    val debug = commonExtension.buildTypes.getByName("debug")
    debug.isMinifyEnabled = false
}