plugins {
    // ✅ 1. Apply Convention Plugins (Handles Base AGP, Kotlin, Compose, Hilt)
    id("composetemplate.android.application")
    id("composetemplate.android.application.compose")
    id("composetemplate.android.hilt")
    id("composetemplate.kotlin.serialization")
    id("composetemplate.android.application.firebase")

    // ✅ 2. Apply Specific Plugins for this App
    alias(libs.plugins.ksp)
    // alias(libs.plugins.androidx.baselineprofile)
}

android {
    namespace = "com.zoewave.probase.photodo.mobile"

    defaultConfig {
        applicationId = "com.zoewave.probase.photodo"
        versionCode = 12
        versionName = "0.0.9"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // ✅ 3. Signing Configs (Kept explicit for security/CI flexibility)
    signingConfigs {
        create("release") {
            // Use safe providers to read local.properties or gradle.properties
            val storeFileProp = providers.gradleProperty("RELEASE_STORE_FILE").orNull
            val storePasswordProp = providers.gradleProperty("RELEASE_STORE_PASSWORD").orNull
            val keyAliasProp = providers.gradleProperty("RELEASE_KEY_ALIAS").orNull
            val keyPasswordProp = providers.gradleProperty("RELEASE_KEY_PASSWORD").orNull

            if (!storeFileProp.isNullOrEmpty() && !storePasswordProp.isNullOrEmpty()) {
                storeFile = file(storeFileProp)
                storePassword = storePasswordProp
                keyAlias = keyAliasProp
                keyPassword = keyPasswordProp
            } else {
                println("⚠️ Release signing keys not found. Release build will not be signed.")
            }
        }
    }

    // ✅ 4. Connect Signing to Release Build
    // (Minification/ProGuard is already handled by your Convention Plugin)
    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            // App-specific resource shrinking (not in common convention)
            isShrinkResources = providers.gradleProperty("isShrinkResources").getOrElse("false").toBoolean()
        }
    }

    // ✅ 5. Android Resources & Lint
    androidResources {
        //localeFilters.addAll(listOf("en", "es"))
    }

    lint {
        baseline = file("lint-baseline.xml")
    }

    // ✅ 6. Enable Build Config (Disabled by default in AGP 9)
    buildFeatures {
        buildConfig = true
    }
    testOptions {
        suites {
            create("journeysTest") {
                assets {
                }
                targets {
                    create("default") {
                    }
                }
                useJunitEngine {
                    inputs += listOf(com.android.build.api.dsl.AgpTestSuiteInputParameters.TESTED_APKS)
                    includeEngines += listOf("journeys-test-engine")
                    enginesDependencies(libs.junit.platform.launcher)
                    enginesDependencies(libs.junit.platform.engine)
                    enginesDependencies(libs.journeys.junit.engine)
                }
                targetVariants += listOf("debug")
            }
        }
    }
}

/* ✅ 7. Secrets Plugin Config
secrets {
    defaultPropertiesFileName = "secrets.defaults.properties"
}

// ✅ 8. Baseline Profile Config
baselineProfile {
    // Suppress AGP 9.0 warning
    warnings {
        maxAgpVersion = false
    }
}*/

dependencies {
    // --- Core Project Modules ---
    implementation(project(":core:data"))    // Shared Data Models
    implementation(project(":core:ui"))
    implementation(project(":core:util"))
    implementation(project(":core:model"))
    implementation(project(":features:camera"))
    implementation(project(":applications:photodo:model"))
    implementation(project(":applications:photodo:db"))
    implementation(project(":applications:photodo:data"))
    // implementation(project(":core:database")) // Uncomment if needed

    // --- PhotoTodo Specific Features ---
    implementation(project(":applications:photodo:apps:mobile:core"))
    implementation(project(":applications:photodo:apps:mobile:features:home"))
    implementation(project(":applications:photodo:apps:mobile:features:tasks"))
    implementation(project(":applications:photodo:apps:mobile:features:settings"))
    implementation(project(":applications:photodo:features:camera"))
    implementation(project(":applications:photodo:features:smartadvice"))
    implementation(project(":features:ai:capture"))
    implementation(project(":features:ai:configuration"))
    // implementation(project(":applications:photodo:features:tasks"))
    // implementation(project(":applications:photodo:features:settings"))

    // --- Shared Feature Modules (Include as needed) ---
    // implementation(project(":features:places"))
    // implementation(project(":feature:camera"))
    // ...

    // --- Third Party & AndroidX ---

    // Nav 3 Libraries from your TOML
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.material3.adaptive.navigation3)
    implementation(libs.androidx.compose.material3.adaptive)
    implementation(libs.androidx.compose.material3.adaptive.layout)
    implementation(libs.androidx.compose.material3.adaptive.navigation)
    implementation(libs.androidx.compose.material3.window.size)

    // Icons
    implementation(libs.androidx.compose.material.icons.extended)

    // Baseline Profile
    implementation(libs.androidx.profileinstaller)

    // Hilt Navigation
    implementation(libs.hilt.navigation.compose)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
}