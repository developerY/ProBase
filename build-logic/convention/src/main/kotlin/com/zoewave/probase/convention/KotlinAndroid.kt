package com.zoewave.probase.convention

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/**
 * Configures basic Android settings (SDK versions, Java version).
 * AGP 9 Rule: Use dot syntax (e.g. compileOptions.sourceCompatibility) instead of blocks { }.
 */
internal fun Project.configureKotlinAndroid(
    commonExtension: CommonExtension,
) {
    commonExtension.apply {
        // 1. Compile SDK (Property of CommonExtension)
        compileSdk = libs.findVersion("android-compileSdk").get().toString().toInt()

        // 2. Min SDK (Property of DefaultConfig)
        // We use 'defaultConfig.minSdk' because the 'defaultConfig { }' block doesn't exist here.
        defaultConfig.minSdk = libs.findVersion("android-minSdk").get().toString().toInt()

        // 3. Java Version (Property of CompileOptions)
        // REQUIRED: AGP 9+ requires Java 17. If you remove this, builds will fail.
        compileOptions.sourceCompatibility = JavaVersion.VERSION_21
        compileOptions.targetCompatibility = JavaVersion.VERSION_21
    }

    // Configure Kotlin JVM Target to match Java 21
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }
}