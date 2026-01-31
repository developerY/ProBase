plugins {
    // ✅ 1. Apply Convention Plugins
    // These handle AGP, Kotlin 2.1, Hilt, and Room setup automatically
    id("composetemplate.android.library")
    id("composetemplate.android.hilt")
    id("composetemplate.android.room")
}

android {
    // ✅ 2. Update Namespace
    namespace = "com.zoewave.probase.core.database"

    defaultConfig {
        // minSdk & compileSdk are handled by the plugin
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    // Removed 'compileOptions', 'buildTypes', 'java' toolchain
    // -> These are all standard in your Convention Plugin now.
}

dependencies {
    // --- Internal Modules ---
    implementation(project(":core:model"))
    // implementation(project(":applications:ashbike:database")) // Keep commented if not ready

    // --- Core Android ---
    implementation(libs.androidx.core.ktx)
    // Note: 'appcompat' and 'material.legacy' are usually not needed in a pure data module
    // unless you are using specific Android resource classes.
    // implementation(libs.androidx.appcompat)
    // implementation(libs.material.legacy)

    // --- Hilt & Room ---
    // Dependencies are AUTOMATICALLY added by:
    // - id("composetemplate.android.hilt")
    // - id("composetemplate.android.room")
    // You do NOT need to list them here again unless you need specific extras.

    // --- Testing ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}