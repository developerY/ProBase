plugins {
    // ✅ 1. Apply Convention Plugins
    id("composetemplate.android.library")
    id("composetemplate.android.library.compose") // Standardizes Compose setup

    // ✅ 2. Module Specific Plugins
    alias(libs.plugins.jetbrains.kotlin.serialization)
}

android {
    // ✅ 3. Update Namespace
    namespace = "com.zoewave.probase.core.ui"

    defaultConfig {
        // compileSdk & minSdk are handled by the Convention Plugin
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    // Removed 'buildTypes', 'compileOptions' -> Handled by Convention Plugin
}

dependencies {
    // --- Core Android ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    // implementation(libs.material.legacy)

    // --- Compose & UI ---
    // Note: 'library.compose' plugin handles the Compiler & BOM logic usually,
    // but explicit dependencies are still needed for the libraries themselves.
    implementation(libs.androidx.compose.material3)

    // ✅ FIXED: Correct accessor matches your TOML (androidx-compose-material-icons-extended)
    implementation(libs.androidx.compose.material.icons.extended)

    // --- Serialization ---
    implementation(libs.kotlinx.serialization.json)

    // --- Testing ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}