plugins {
    // ✅ 1. Apply Convention Plugins
    id("composetemplate.android.library")
    id("composetemplate.android.hilt")
    id("composetemplate.android.room") // Automatically adds Room, KSP, and Schema config
}

android {
    // ✅ 2. Update Namespace
    namespace = "com.zoewave.probase.ashbike.database"

    defaultConfig {
        // minSdk & compileSdk are handled by the Convention Plugin
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    // Removed 'buildTypes', 'compileOptions' -> Handled by Convention Plugin
}

dependencies {
    // --- Internal Modules ---
    implementation(project(":core:model"))

    // --- Core Android ---
    implementation(libs.androidx.core.ktx)
    // Optional: Only include these if you specifically need Android View classes in your database module
    // implementation(libs.androidx.appcompat)
    // implementation(libs.material.legacy)

    // --- DataStore ---
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore.core)

    // --- Hilt & Room ---
    // Dependencies are AUTOMATICALLY added by the plugins above:
    // - hilt-android, hilt-compiler (ksp)
    // - room-runtime, room-ktx, room-compiler (ksp)

    // --- Testing ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}