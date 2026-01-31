plugins {
    // ✅ 1. Apply Convention Plugins
    id("composetemplate.android.library")
    id("composetemplate.android.library.compose") // Adds Compose capability

    // ✅ 2. Module Specific Plugins
    alias(libs.plugins.jetbrains.kotlin.serialization)
}

android {
    namespace = "com.zoewave.probase.core.model"

    defaultConfig {
        // minSdk & compileSdk are handled by the Convention Plugin
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    // Removed 'buildTypes' & 'compileOptions' -> Handled by Convention Plugin
}

dependencies {
    // --- Core Android ---
    implementation(libs.androidx.core.ktx)
    // Note: 'appcompat' & 'material.legacy' are rarely needed in pure Compose/Model modules,
    // but kept here for compatibility with your existing code.
    // implementation(libs.androidx.appcompat)
    // implementation(libs.material.legacy)

    // --- Compose & UI ---
    // The 'library.compose' plugin sets up the BOM and compiler,
    // but we usually add specific libraries we need:
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // --- Data & Collections ---
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.kotlinx.serialization.json)

    // --- Domain Specifics (Health & Maps) ---
    implementation(libs.androidx.health.connect.client)
    implementation(libs.google.maps.compose)

    // --- Testing ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}