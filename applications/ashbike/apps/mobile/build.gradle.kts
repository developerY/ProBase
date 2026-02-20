plugins {
    // ✅ 1. Apply Convention Plugins (Handles Base AGP, Kotlin, Compose, Hilt)
    id("composetemplate.android.application")
    id("composetemplate.android.application.compose")
    id("composetemplate.android.hilt")

    // ✅ 2. Apply Specific Plugins for this App
    alias(libs.plugins.ksp)
    alias(libs.plugins.mapsplatform.secrets)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.firebase.crashlytics)
    // alias(libs.plugins.androidx.baselineprofile)
}

android {
    namespace = "com.zoewave.probase.ashbike.mobile"

    defaultConfig {
        applicationId = "com.zoewave.probase.ashbike.mobile"
        versionCode = 1
        versionName = "0.0.1"

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
    // --- Project Modules ---
    implementation(project(":core:data"))    // Shared Data Models
    implementation(project(":core:ui"))
    implementation(project(":core:util"))
    implementation(project(":core:model"))

    // AshBike Specific Features
    implementation(project(":applications:ashbike:features:main"))
    implementation(project(":applications:ashbike:apps:mobile:features:home"))
    implementation(project(":applications:ashbike:apps:mobile:features:rides"))
    implementation(project(":applications:ashbike:apps:mobile:features:settings"))

    implementation(project(":applications:ashbike:database"))

    implementation(project(":features:places"))
    /* Shared Features (Assuming these paths exist in your monorepo)
    implementation(project(":feature:listings"))
    implementation(project(":feature:camera"))
    implementation(project(":feature:places"))
    implementation(project(":feature:health")) // Fixed typo from 'heatlh'
    implementation(project(":feature:maps"))
    implementation(project(":feature:ble"))
    implementation(project(":feature:alarm"))
    implementation(project(":feature:weather"))
    implementation(project(":feature:qrscanner"))
    implementation(project(":feature:nfc"))
    implementation(project(":feature:ml"))

    // Core Modules
    implementation(project(":core:data"))
    implementation(project(":core:database"))
    implementation(project(":core:model"))
    implementation(project(":core:ui"))
    implementation(project(":core:util"))*/

    // --- Third Party & AndroidX ---


    // Nav 3 Libraries from your TOML
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)

    // Icons
    implementation(libs.androidx.compose.material.icons.extended)


    // Serialization for Keys
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)

    // Optional: ViewModel support (You have this in TOML, good to include)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)

    // The library containing the Adaptive APIs
    implementation(libs.androidx.material3.adaptive.navigation3)


    // Optional: ViewModel support (You have this in TOML, good to include)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)

    // Maps
    implementation(libs.google.maps.compose)

    // Health Connect
    implementation(libs.androidx.health.connect.client)

    // Baseline Profile
    implementation(libs.androidx.profileinstaller)

    // Note: Hilt, Compose, and Core KTX are already added by the
    // 'composetemplate' plugins, but adding specific libraries like
    // icons or navigation explicitly is fine for clarity.
    implementation(libs.androidx.compose.material.icons.extended)
    //implementation(libs.androidx.navigation.compose)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    //debugImplementation(libs.androidx.ui.tooling)
    //debugImplementation(libs.androidx.ui.test.manifest)
}