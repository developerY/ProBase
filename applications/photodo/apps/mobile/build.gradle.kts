plugins {
    // ✅ 1. Apply Convention Plugins (Handles Base AGP, Kotlin, Compose, Hilt)
    id("composetemplate.android.application")
    id("composetemplate.android.application.compose")
    id("composetemplate.android.hilt")

    // ✅ 2. Apply Specific Plugins for this App
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.firebase.crashlytics)
    // alias(libs.plugins.androidx.baselineprofile)
}

android {
    namespace = "com.zoewave.probase.photodo.mobile"

    defaultConfig {
        applicationId = "com.zoewave.probase.photodo"
        versionCode = 1
        versionName = "1.0"

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
    // implementation(project(":core:database")) // Uncomment if needed

    // --- PhotoTodo Specific Features ---
    implementation(project(":applications:photodo:apps:mobile:features:home"))
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

    // Icons
    implementation(libs.androidx.compose.material.icons.extended)

    // Serialization for Keys
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)

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