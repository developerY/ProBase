plugins {
    // 1. Apply Convention Plugins (Library, Compose, Hilt)
    id("composetemplate.android.library")
    id("composetemplate.android.library.compose")
    id("composetemplate.android.hilt")

    // ✅ Required for Type-Safe Navigation & Nav3
    alias(libs.plugins.jetbrains.kotlin.serialization)
}

android {
    // Unique namespace for the Home Feature
    namespace = "com.zoewave.ashbike.mobile.home"
}

dependencies {
    // --- Shared Projects ---
    implementation(project(":core:model"))
    implementation(project(":core:ui"))
    implementation(project(":core:network"))
    implementation(project(":core:data"))

    // Database (If Home needs direct DB access, otherwise access via :core:data)
    implementation(project(":applications:ashbike:database"))
    implementation(project(":applications:ashbike:model"))
    implementation(project(":applications:ashbike:data"))
    implementation(project(":applications:ashbike:features:main"))

    // --- Serialization (The backbone of Nav3) ---
    implementation(libs.kotlinx.serialization.json)

    // --- Navigation 3 (Strict) ---
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)

    // --- Maps & Location (For the Dashboard/Speedometer) ---
    implementation(libs.google.play.services.location)
    implementation(libs.google.maps.compose)

    // --- 4. Standard UI & Compose ---
    // Note: 'library.compose' plugin adds the BOM and basic UI/Tooling automatically.
    // We only add specific extras here.
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.material3) // If using M3 components alongside Glimmer
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // --- 5. Hilt ---
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    // Note: The Hilt Convention Plugin handles 'ksp(libs.hilt.compiler)' automatically

    // --- Lifecycle ---
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.lifecycle.service)

    // --- UI Components ---
    // ✅ Icons for the dashboard
    implementation(libs.androidx.compose.material.icons.extended)

    // --- Collections ---
    implementation(libs.kotlinx.collections.immutable)

    // --- Testing ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}