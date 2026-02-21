plugins {
    // ✅ 1. Apply Convention Plugins
    id("composetemplate.android.application")
    id("composetemplate.android.application.compose")
    id("composetemplate.android.hilt")

    // ✅ 2. Specific Plugins for Wear
    alias(libs.plugins.ksp) // Explicitly needed for Room
}

android {
    namespace = "com.zoewave.probase.ashbike.wear"

    defaultConfig {
        applicationId = "com.zoewave.probase.ashbike.wear"
        versionCode = 1
        versionName = "1.0"

        // Wear OS specifically needs minSdk 30 for Health Services
        minSdk = 35
    }

    // ✅ STEP 1: Define the "release" signing config here
    signingConfigs {
        create("release") {
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
                println("⚠️ Wear OS Release signing keys not found.")
            }
        }
    }

    // ✅ 3. Build Types
    // (Minification/ProGuard is already handled by your Convention Plugin)
    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")

            // App-specific resource shrinking (not in common convention)
            isShrinkResources = providers.gradleProperty("isShrinkResources").getOrElse("false").toBoolean()
        }
    }

    // ✅ 4. Wear Specific SDK Old and not needed
    // useLibrary("android.wear")

    // Enable Build Config if you need it (disabled by default in AGP 9)
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    // --- Project Modules ---
    // Share the exact same logic/database as Mobile!
    implementation(project(":applications:ashbike:features:main"))
    implementation(project(":applications:ashbike:model"))
    // implementation(project(":applications:ashbike:database"))

    // module
    implementation(project(":applications:ashbike:apps:wear:features:home"))
    implementation(project(":applications:ashbike:apps:wear:features:rides"))
    implementation(project(":applications:ashbike:apps:wear:features:settings"))

    // Core
    implementation(project(":core:model"))
    implementation(project(":core:data"))
    implementation(project(":core:util"))

    // --- Wear OS Specifics ---

    // Health Services (Tracking rides)
    implementation(libs.androidx.health.services.client)

    // Ongoing Activity (The running icon)
    implementation(libs.androidx.wear.ongoing)

    // UI & Navigation
    implementation(libs.androidx.navigation3.runtime) // Core state/list logic
    implementation(libs.androidx.wear.compose.navigation3) // Wear-specific swipe-to-dismiss UI

    implementation(libs.androidx.wear.compose.material3)
    implementation(libs.androidx.wear.compose.foundation)

    implementation(libs.androidx.core.splashscreen)

    implementation(libs.androidx.compose.material.icons.extended)

    // Google Services
    implementation(libs.google.play.services.wearable)

    // Horologist (Google's "Jetpack" for Wear)
    implementation(libs.horologist.compose.tools)
    implementation(libs.horologist.tiles)

    // Tiles & Complications
    implementation(libs.androidx.watchface.complications.data.source.ktx)

    // Permissions
    implementation(libs.google.accompanist.permissions)

    // --- Standard Android Components ---

    // Hilt (Navigation Compose integration)
    implementation(libs.hilt.navigation.compose)

    // Room (Local Database)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.navigation3.ui)
    ksp(libs.room.compiler)

    // Lifecycle
    implementation(libs.androidx.lifecycle.service)

    // Note: Core Compose, Hilt, and KTX are added automatically by plugins

    // Testing & Debug
    //debugImplementation(libs.androidx.ui.tooling)
    //debugImplementation(libs.androidx.ui.test.manifest)
    debugImplementation(libs.androidx.tiles.tooling)
}