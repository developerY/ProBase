//namespace = "com.zoewave.probase.mobile.features.settings"
plugins {
    // ✅ 1. Apply Convention Plugins (Library, Compose, Hilt)
    id("composetemplate.android.library")
    id("composetemplate.android.library.compose")
    id("composetemplate.android.hilt")
    id("composetemplate.android.firebase") // Common Firebase dependencies (Library safe)

    // ✅ 2. Required for Type-Safe Navigation & Nav3
    id("composetemplate.kotlin.serialization")
}

android {
    // Unique namespace for the PhotoTodo Home Feature
    namespace = "com.zoewave.probase.photodo.mobile.features.settings"

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    // --- Firebase & Google Services ---
    // Inherited from composetemplate.android.firebase convention plugin

    // --- Shared Core Projects ---
    // implementation(project(":core:model"))
    // implementation(project(":core:ui"))
    // implementation(project(":core:network"))
    // implementation(project(":core:data"))
    implementation(project(":applications:photodo:db"))
    implementation(project(":applications:photodo:model"))
    implementation(project(":applications:photodo:apps:mobile:core"))
    implementation(project(":features:smartcapture"))
    implementation(project(":features:ai:configuration"))

    // --- Serialization (The backbone of Nav3) ---
    implementation(libs.kotlinx.serialization.json)

    // --- Navigation 3 (Strict) ---
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)

    // --- Standard UI & Compose ---
    // Note: 'library.compose' plugin adds the BOM and basic UI/Tooling automatically.
    // We only add specific extras here.
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.activity.compose)

    // --- Hilt ---
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    // Note: The Hilt Convention Plugin handles 'ksp(libs.hilt.compiler)' automatically

    // --- Lifecycle ---
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.lifecycle.service)

    // --- Collections ---
    implementation(libs.kotlinx.collections.immutable)

    // --- Testing ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}